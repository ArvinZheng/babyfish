/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.persistence.tool.path;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.tools.JavaFileObject;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.BinaryFunc;
import org.babyfish.lang.Func;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.reflect.Strings;
import org.babyfish.persistence.path.CollectionFetchType;
import org.babyfish.persistence.path.FetchPath;
import org.babyfish.persistence.path.FetchPathWrapper;
import org.babyfish.persistence.path.GetterType;
import org.babyfish.persistence.path.QueryPath;
import org.babyfish.persistence.path.QueryPaths;
import org.babyfish.persistence.path.SimpleOrderPath;
import org.babyfish.persistence.path.SimpleOrderPathWrapper;
import org.babyfish.persistence.path.TypedFetchPath;
import org.babyfish.persistence.path.TypedQueryPath;
import org.babyfish.persistence.path.TypedSimpleOrderPath;
import org.babyfish.util.LazyResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
@SupportedAnnotationTypes({ "javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embeddable" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class TypedQueryPathProcessor extends AbstractProcessor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TypedQueryPathProcessor.class);
    
    private static final Class<?> DEFAULT_ANY_ANNOTATION_TYPE;
    
    private static final String NAME_POSTFIX = "__";
    
    private static final String LINE_SPERATOR = System.getProperty("line.separator", "\n");
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private Class<? extends Annotation> anyAnnotationType;
    
    public TypedQueryPathProcessor() {
        this(null);
    }
    
    @SuppressWarnings("unchecked")
    protected TypedQueryPathProcessor(Class<? extends Annotation> anyAnnotationType) {
        if (anyAnnotationType == null) {
            this.anyAnnotationType = (Class<? extends Annotation>)DEFAULT_ANY_ANNOTATION_TYPE;
        } else {
            this.anyAnnotationType = anyAnnotationType;
        }
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        try {
            return this.processImpl(annotations, roundEnv); 
        } catch (RuntimeException | Error ex) {
            LOGGER.error(
                    "Failed to process because an exception raised",
                    ex);
            ex.printStackTrace();
            throw ex;
        }
    }
    
    private boolean processImpl(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        if ((roundEnv.processingOver())) {
            LOGGER.info("Skip the process because the processing is over");
            return false;
        }
        if (annotations.isEmpty()) {
            LOGGER.info("Skip the process because there is no annotations");
            return false;
        }
        Map<String, MetaClass> metaClasses = new HashMap<>();
        for (Element element : roundEnv.getRootElements()) {
            if (containsAnyAnnotation(element, Entity.class, Embeddable.class, MappedSuperclass.class)) {
                TypeElement typeElement = (TypeElement)element;
                if (typeElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                    throw new IllegalProgramException("Entity, Embeddable, MappedSuperClass can not be nested class.");
                }
                MetaClass entity = new MetaClass(typeElement, this.anyAnnotationType);
                metaClasses.put(entity.getName(), entity);
            }
        }
        if (metaClasses.isEmpty()) {
            LOGGER.info(
                    "Skip the process because there is no class that is marked by \"@" +
                    Entity.class.getName() +
                    ", " +
                    Embeddable.class.getName() +
                    "\" and \"@" +
                    MappedSuperclass.class.getName() +
                    "\"");
            return false;
        }
        for (MetaClass entity : metaClasses.values()) {
            entity.secondaryPass(metaClasses);
        }
        
        for (MetaClass entity : metaClasses.values()) {
            try {
                this.generate(entity);
            } catch (Throwable ex) {
                throw new IllegalProgramException(
                        "Failed to generate the fetch model for \"" +
                        entity.getName() +
                        "\"",
                        ex);
            }
        }
        
        return false;
    }
    
    private void generate(MetaClass metaClass) throws IOException {
        JavaFileObject fo =
                this
                .processingEnv
                .getFiler()
                .createSourceFile(metaClass.getName() + NAME_POSTFIX);
        try (Writer writer = fo.openWriter()) {
            
            writer.write("package ");
            writer.write(metaClass.getPackageName());
            writer.write(";");
            writer.write(LINE_SPERATOR);
            writer.write(LINE_SPERATOR);
            
            this.generateImports(metaClass, writer);
            
            writer.write("@Generated(\"");
            writer.write(TypedQueryPathProcessor.class.getName());
            writer.write("\")");
            writer.write(LINE_SPERATOR);
            writer.write("public abstract class ");
            writer.write(metaClass.getSimpleName());
            writer.write(NAME_POSTFIX);
            if (metaClass.isEntity()) {
                writer.write(" implements TypedQueryPath<");
                writer.write(metaClass.getSimpleName());
                writer.write(">");
            }
            writer.write(" {");
            writer.write(LINE_SPERATOR);
            
            if (metaClass.isEntity()) {
                this.generateSerialVersionUID(metaClass, writer, 0, 1);
            }
            CodeBuilder builder = new CodeBuilder(1);
            if (metaClass.isEntity()) {
                this.generateMembers(metaClass, builder);
                this.generateFetchPathImpl(metaClass, builder);
                this.generateSimpleOrderPathImpl(metaClass, builder);
                this.generateFetchPathBuilderImpl(metaClass, builder);
            }
            this.generateSimpleOrderPathBuilderImpl(metaClass, builder);
            writer.write(builder.toString());
            
            writer.write("}");
            writer.write(LINE_SPERATOR);
        }
    }
    
    private void generateImports(MetaClass metaClass, Writer writer) throws IOException {
        
        writer.write("import javax.annotation.Generated;");
        writer.write(LINE_SPERATOR);
        
        NavigableSet<String> importedClassNames = new TreeSet<>();
        importedClassNames.add(BinaryFunc.class.getName());
        importedClassNames.add(TypedSimpleOrderPath.class.getName());
        importedClassNames.add(SimpleOrderPath.class.getName());
        if (metaClass.isEntity()) {
            for (MetaAssociation association : metaClass.getAssociations().values()) {
                if (association.isCollection()) {
                    importedClassNames.add(CollectionFetchType.class.getName());
                    break;
                }
            }
            if (!metaClass.getAssociations().isEmpty()) {
                importedClassNames.add(GetterType.class.getName());
            }
            importedClassNames.add(Func.class.getName());
            importedClassNames.add(FetchPath.class.getName());
            importedClassNames.add(FetchPathWrapper.class.getName());   
            importedClassNames.add(QueryPath.class.getName());
            importedClassNames.add(QueryPaths.class.getName());
            importedClassNames.add(SimpleOrderPathWrapper.class.getName());
            importedClassNames.add(TypedQueryPath.class.getName());
            importedClassNames.add(TypedFetchPath.class.getName());
        }
        
        for (MetaAssociation association : metaClass.getAssociations().values()) {
            MetaClass otherClass = (MetaClass)association.getRelatedMetaClass();
            if (metaClass != otherClass && 
                    !Nulls.isNullOrEmpty(metaClass.getPackageName()) && 
                    !metaClass.getPackageName().equals(otherClass.getPackageName())) {
                importedClassNames.add(otherClass.getName());
            }
        }
        if (importedClassNames.isEmpty()) {
            return;
        }
        
        String toppestPackage = "java";
        for (String importedClassName : importedClassNames) {
            int dotIndex = importedClassName.indexOf('.');
            if (dotIndex != -1) {
                String newToppestPackage = importedClassName.substring(0, dotIndex);
                if (!toppestPackage.equals(newToppestPackage)) {
                    toppestPackage = newToppestPackage;
                    writer.write(LINE_SPERATOR);
                }
            }
            writer.write("import ");
            writer.write(importedClassName);
            writer.write(";");
            writer.write(LINE_SPERATOR);
        }
        writer.write(LINE_SPERATOR);
    }

    private void generateSerialVersionUID(MetaClass metaClass, Writer writer, int shift, int tabCount) throws IOException {
        long serialVersionUID = 0;
        for (MetaAssociation association : metaClass.getAssociations().values()) {
            serialVersionUID += ((long)association.getName().hashCode() << 32) | association.getRelatedMetaClass().getName().hashCode();
        }
        serialVersionUID <<= shift;
        writer.write(LINE_SPERATOR);
        for (int i = tabCount - 1; i >= 0; i--) {
            writer.write('\t');
        }
        writer.write("private static final long serialVersionUID = ");
        writer.write(Long.toString(serialVersionUID));
        writer.write("L;");
        writer.write(LINE_SPERATOR);
    }
    
    private long getSerialVersionUID(MetaClass metaClass, String nestedSimpleClassName) throws IOException {
        long serialVersionUID = nestedSimpleClassName != null ? nestedSimpleClassName.hashCode() : 0;
        for (MetaProperty property : metaClass.getProperties().values()) {
            serialVersionUID += serialVersionUID * 31 + property.getName().hashCode();
        }
        return serialVersionUID;
    }
    
    private void generateMembers(MetaClass metaClass, CodeBuilder builder) throws IOException {
        
        String entityName = metaClass.getSimpleName();
        
        builder
        .appendLine()
        .append("private static final BinaryFunc<SimpleOrderPath.Builder, Boolean, SimpleOrderPathImpl> SIMPLE_ORDER_PATH_CREATOR = ");
        try (BlockScope intializationScope = new BlockScope(builder, null, null)) {
            builder.append("new BinaryFunc<SimpleOrderPath.Builder, Boolean, SimpleOrderPathImpl>()");
            try (BlockScope anonymousScope = new BlockScope(builder, false)) {
                builder
                .appendLine("@Override")
                .append("public SimpleOrderPathImpl run(SimpleOrderPath.Builder builder, Boolean desc)");
                try (BlockScope lambdaScope = new BlockScope(builder)) {
                    builder
                    .append("if (desc.booleanValue())");
                    try (BlockScope ifScope = new BlockScope(builder)) {
                        builder.appendLine("return new SimpleOrderPathImpl(builder.desc());");
                    }
                    builder.appendLine("return new SimpleOrderPathImpl(builder.asc());");
                }
            }
            builder.append(";");
        }
        
        builder
        .appendLine()
        .appendLine("@Override")
        .append("public Class<")
        .append(entityName)
        .append("> getRootType()");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return ")
            .append(entityName)
            .appendLine(".class;");
        }
        
        builder
        .appendLine()
        .append("public static FetchPathBuilder<")
        .append(entityName)
        .append(", FetchPathImpl> begin()");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return new FetchPathBuilder<")
            .append(entityName)
            .append(", ")
            .append("FetchPathImpl>");
            try (BlockScope parameterBlock = new BlockScope(builder, "(", ");")) {
                builder
                .appendLine("QueryPaths.begin(), ")
                .append("new Func<FetchPath.Builder, FetchPathImpl>()");
                try (BlockScope anonymousClassScope = new BlockScope(builder)) {
                    builder
                    .appendLine("@Override")
                    .append("public FetchPathImpl run(FetchPath.Builder builder)");
                    try (BlockScope lambdaScope = new BlockScope(builder)) {
                        builder.appendLine("return new FetchPathImpl(builder.end());");
                    }
                }
            }
        }
        
        builder
        .appendLine()
        .append("public static SimpleOrderPathBuilder<")
        .append(entityName)
        .append(", SimpleOrderPathImpl> preOrderBy()");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return new SimpleOrderPathBuilder<")
            .append(entityName)
            .append(", ")
            .appendLine("SimpleOrderPathImpl>(QueryPaths.preOrderBy(), SIMPLE_ORDER_PATH_CREATOR);");
        }
        
        builder
        .appendLine()
        .append("public static SimpleOrderPathBuilder<")
        .append(entityName)
        .append(", SimpleOrderPathImpl> postOrderBy()");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return new SimpleOrderPathBuilder<")
            .append(entityName)
            .append(", ")
            .appendLine("SimpleOrderPathImpl>(QueryPaths.postOrderBy(), SIMPLE_ORDER_PATH_CREATOR);");
        }
        
        builder
        .appendLine()
        .append("public static ")
        .append(metaClass.getSimpleName())
        .append(NAME_POSTFIX)
        .append("[] compile(String queryPath)");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder.appendLine("return compile(new String[]{ queryPath });");
        }
        
        builder
        .appendLine()
        .append("public static ")
        .append(metaClass.getSimpleName())
        .append(NAME_POSTFIX)
        .append("[] compile(String[] queryPaths)");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .appendLine("QueryPath[] arr = QueryPaths.compile(queryPaths);")
            .append(metaClass.getSimpleName())
            .append(NAME_POSTFIX)
            .append("[] typedQueryPaths = new ")
            .append(metaClass.getSimpleName())
            .append(NAME_POSTFIX)       
            .appendLine("[arr.length];")
            .append("for (int i = arr.length - 1; i >= 0; i--)");
            try (BlockScope forScope = new BlockScope(builder)) {
                builder.append("if (arr[i] instanceof FetchPath)");
                try (BlockScope ifScope = new BlockScope(builder, false)) {
                    builder.appendLine("typedQueryPaths[i] = new FetchPathImpl((FetchPath)arr[i]);");
                }
                builder.append(" else");
                try (BlockScope elseScope = new BlockScope(builder)) {
                    builder.appendLine("typedQueryPaths[i] = new SimpleOrderPathImpl((SimpleOrderPath)arr[i]);");
                }
            }
            builder.appendLine("return typedQueryPaths;");
        }
    }
    
    private void generateFetchPathImpl(MetaClass metaClass, CodeBuilder builder) throws IOException {
        
        String entityName = metaClass.getSimpleName();
        
        builder
        .appendLine()
        .append("public static class FetchPathImpl extends ")
        .append(entityName)
        .append(NAME_POSTFIX)
        .append(" implements TypedFetchPath<")
        .append(entityName)
        .append(">, FetchPathWrapper");
        
        try (BlockScope classScope = new BlockScope(builder)) {
            builder
            .appendLine()
            .append("private static final long serialVersionUID = ")
            .append(Long.toString(getSerialVersionUID(metaClass, "FetchPathImpl")))
            .appendLine("L;")
            .appendLine()
            .appendLine("private FetchPath fetchPath;")
            .appendLine()
            .append("FetchPathImpl(FetchPath fetchPath)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("this.fetchPath = fetchPath;");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public Node getFirstNode()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.fetchPath.getFirstNode();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public FetchPath unwrap()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.fetchPath;");
            }
        }
    }
    
    private void generateFetchPathBuilderImpl(MetaClass metaClass, CodeBuilder builder) throws IOException {
        
        builder
        .appendLine()
        .append("public static class FetchPathBuilder<R, P extends TypedFetchPath<R>> extends ");
        if (metaClass.getSuperMetaClass() != null && metaClass.getSuperMetaClass().isEntity()) {
            builder
            .append(metaClass.getSuperMetaClass().getSimpleName())
            .append(NAME_POSTFIX)
            .append(".FetchPathBuilder<R, P> ");
        } else {
            builder.append("TypedFetchPath.TypedBuilder<R, P>");
        }
        
        try (BlockScope classScope = new BlockScope(builder)) {
            
            builder
            .appendLine()
            .append("FetchPathBuilder")
            .append("(FetchPath.Builder builder, Func<FetchPath.Builder, P> pathCreator)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("super(builder, pathCreator);");
            }
            
            for (MetaAssociation association : metaClass.getAssociations().values()) {
                
                String targetEntityName = association.getRelatedMetaClass().getSimpleName();
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityName)
                .append(NAME_POSTFIX)
                .append(".FetchPathBuilder<R, P> ")
                .append(association.getName())
                .append("()");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityName)
                    .append(NAME_POSTFIX)
                    .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\"), this.pathCreator);");
                }
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityName)
                .append(NAME_POSTFIX)
                .append(".FetchPathBuilder<R, P> ")
                .append(association.getName())
                .append("(GetterType getterType)");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityName)
                    .append(NAME_POSTFIX)
                    .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\", getterType), this.pathCreator);");
                }
                
                if (association.isCollection()) {
                    
                    builder
                    .appendLine()
                    .append("public ")
                    .append(targetEntityName)
                    .append(NAME_POSTFIX)
                    .append(".FetchPathBuilder<R, P> ")
                    .append(association.getName())
                    .append("(CollectionFetchType collectionFetchType)");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                        .append(targetEntityName)
                        .append(NAME_POSTFIX)
                        .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                        .append(association.getName())
                        .appendLine("\", collectionFetchType), this.pathCreator);");
                    }
                    
                    builder
                    .appendLine()
                    .append("public ")
                    .append(targetEntityName)
                    .append(NAME_POSTFIX)
                    .append(".FetchPathBuilder<R, P> ")
                    .append(association.getName())
                    .append("(GetterType getterType, CollectionFetchType collectionFetchType)");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                        .append(targetEntityName)
                        .append(NAME_POSTFIX)
                        .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                        .append(association.getName())
                        .appendLine("\", getterType, collectionFetchType), this.pathCreator);");
                    }
                }
            }
            
            for (MetaScalar scalar : metaClass.getScalars().values()) {
                if (scalar.isLazy()) {
                    builder
                    .appendLine()
                    .append("public ")
                    .append("TypedFetchPath.TypedBuilder<R, P> ")
                    .append(scalar.getName())
                    .append("()");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new TypedFetchPath.TypedBuilder<R, P>(this.builder.get(\"")
                        .append(scalar.getName())
                        .appendLine("\"), this.pathCreator);");
                    }
                }
            }
        }
    }
    
    private void generateSimpleOrderPathImpl(MetaClass metaClass, CodeBuilder builder) throws IOException {
        
        String entityName = metaClass.getSimpleName();
        
        builder
        .appendLine()
        .append("public static class SimpleOrderPathImpl extends ")
        .append(entityName)
        .append(NAME_POSTFIX)
        .append(" implements TypedSimpleOrderPath<")
        .append(entityName)
        .append(">, SimpleOrderPathWrapper");
        
        try (BlockScope classScope = new BlockScope(builder)) {
            builder
            .appendLine()
            .append("private static final long serialVersionUID = ")
            .append(Long.toString(getSerialVersionUID(metaClass, "SimpleOrderPathImpl")))
            .appendLine("L;")
            .appendLine()
            .appendLine("private SimpleOrderPath simpleOrderPath;")
            .appendLine()
            .append("SimpleOrderPathImpl(SimpleOrderPath simpleOrderPath)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("this.simpleOrderPath = simpleOrderPath;");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public boolean isPost()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath.isPost();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public boolean isDesc()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath.isDesc();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public Node getFirstNode()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath.getFirstNode();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public SimpleOrderPath unwrap()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath;");
            }
        }
    }
    
    private void generateSimpleOrderPathBuilderImpl(MetaClass metaClass, CodeBuilder builder) throws IOException {
        
        builder
        .appendLine()
        .append("public static class SimpleOrderPathBuilder<R, P extends TypedSimpleOrderPath<R>> extends ");
        if (metaClass.getSuperMetaClass() != null) {
            builder
            .append(metaClass.getSuperMetaClass().getSimpleName())
            .append(NAME_POSTFIX)
            .append(".SimpleOrderPathBuilder<R, P> ");
        } else {
            builder.append("TypedSimpleOrderPath.TypedBuilder<R, P>");
        }
        
        try (BlockScope classScope = new BlockScope(builder)) {
            
            builder
            .appendLine()
            .append("SimpleOrderPathBuilder")
            .append("(SimpleOrderPath.Builder builder, BinaryFunc<SimpleOrderPath.Builder, Boolean, P> pathCreator)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("super(builder, pathCreator);");
            }
            
            for (MetaAssociation association : metaClass.getAssociations().values()) {
                
                String targetEntityName = association.getRelatedMetaClass().getSimpleName();
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityName)
                .append(NAME_POSTFIX)
                .append(".SimpleOrderPathBuilder<R, P> ")
                .append(association.getName())
                .append("()");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityName)
                    .append(NAME_POSTFIX)
                    .append(".SimpleOrderPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\"), this.pathCreator);");
                }
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityName)
                .append(NAME_POSTFIX)
                .append(".SimpleOrderPathBuilder<R, P> ")
                .append(association.getName())
                .append("(GetterType getterType)");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityName)
                    .append(NAME_POSTFIX)
                    .append(".SimpleOrderPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\", getterType), this.pathCreator);");
                }
            }
            
            for (MetaScalar scalar : metaClass.getScalars().values()) {
                if (scalar.isEmbedded()) {
                    String embeddableName = scalar.getRelatedMetaClass().getSimpleName();
                    builder
                    .appendLine()
                    .append("public ")
                    .append(embeddableName)
                    .append(NAME_POSTFIX)
                    .append(".SimpleOrderPathBuilder<R, P> ")
                    .append(scalar.getName())
                    .append("()");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                        .append(embeddableName)
                        .append(NAME_POSTFIX)
                        .append(".SimpleOrderPathBuilder<R, P>(this.builder.get(\"")
                        .append(scalar.getName())
                        .appendLine("\"), this.pathCreator);");
                    }
                } else {
                    builder
                    .appendLine()
                    .append("public TypedSimpleOrderPath.TypedBuilder<R, P> ")
                    .append(scalar.getName())
                    .append("()");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new TypedSimpleOrderPath.TypedBuilder<R, P>(this.builder.get(\"")
                        .append(scalar.getName())
                        .appendLine("\"), this.pathCreator);");
                    }
                }
            }
        }
    }
    
    @SafeVarargs
    private static boolean containsAnyAnnotation(
            Element element, 
            Class<? extends Annotation> ... annotationTypes) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            for (Class<?> annotationType : annotationTypes) {
                if (annotationType != null && 
                        annotationType.getName().equals(mirror.getAnnotationType().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Object getAnnotationValue(
            Element element, 
            Class<? extends Annotation> annotationType,
            String parameterName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (annotationType.getName().equals(mirror.getAnnotationType().toString())) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                    mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals(parameterName)) {
                        return entry.getValue().accept(new SimpleAnnotationVisitor(), null);
                    }
                }
            }
        }
        return null;
    }
    
    private static <E extends Enum<E>> E getAnnotationEnumValue(
            Element element, 
            Class<? extends Annotation> annotationType,
            String parameterName,
            Class<E> enumType) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (annotationType.getName().equals(mirror.getAnnotationType().toString())) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                    mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals(parameterName)) {
                        return (E)Enum.valueOf(
                                enumType, 
                                (String)entry.getValue().accept(new SimpleAnnotationVisitor(), null)
                        );
                    }
                }
            }
        }
        return null;
    }

    private static class MetaClass {
        
        private boolean entity;
        
        private String name;
        
        private String simpleName;
        
        private String packageName;
        
        private XOrderedMap<String, MetaProperty> properties;
        
        private XOrderedMap<String, MetaAssociation> associations;
        
        private XOrderedMap<String, MetaScalar> scalars;
        
        private Object supperMetaClass;
        
        private AccessType accessType;
        
        MetaClass(TypeElement typeElement, Class<? extends Annotation> anyAnnotationType) {
            this.entity = containsAnyAnnotation(typeElement, Entity.class);
            this.name = typeElement.getQualifiedName().toString();
            this.simpleName = typeElement.getSimpleName().toString();
            for (Element enclosing = typeElement.getEnclosingElement();
                    ;
                    enclosing = enclosing.getEnclosingElement()) {
                if (enclosing.getKind() == ElementKind.PACKAGE) {
                    this.packageName = ((PackageElement)enclosing).getQualifiedName().toString();
                    break;
                }
            }
            this.accessType = getDefaultAccessType(typeElement);
            
            XOrderedMap<String, MetaProperty> properties = new LinkedHashMap<>();
            XOrderedMap<String, MetaAssociation> associations = new LinkedHashMap<>();
            XOrderedMap<String, MetaScalar> scalars = new LinkedHashMap<>();
            for (Element element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                MetaProperty property = MetaProperty.of(this, element, anyAnnotationType);
                if (property instanceof MetaScalar) {
                    MetaScalar scalar = (MetaScalar)property;
                    properties.put(scalar.getName(), scalar);
                    scalars.put(scalar.getName(), scalar);
                } else if (property instanceof MetaAssociation) {
                    MetaAssociation association = (MetaAssociation)property;
                    properties.put(association.getName(), association);
                    associations.put(association.getName(), association);
                }
            }
            for (Element element : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                MetaProperty property = MetaProperty.of(this, element, anyAnnotationType);
                if (property instanceof MetaScalar) {
                    MetaScalar scalar = (MetaScalar)property;
                    properties.put(scalar.getName(), scalar);
                    scalars.put(scalar.getName(), scalar);
                } else if (property instanceof MetaAssociation) {
                    MetaAssociation association = (MetaAssociation)property;
                    properties.put(association.getName(), association);
                    associations.put(association.getName(), association);
                }
            }
            this.properties = MACollections.unmodifiable(properties);
            this.associations = MACollections.unmodifiable(associations);
            this.scalars = MACollections.unmodifiable(scalars);
            this.supperMetaClass = typeElement.getSuperclass();
        }
        
        public boolean isEntity() {
            return this.entity;
        }

        public String getName() {
            return this.name;
        }
        
        public String getSimpleName() {
            return this.simpleName;
        }
        
        public String getPackageName() {
            return this.packageName;
        }
        
        public AccessType getAccessType() {
            return this.accessType;
        }
        
        public MetaClass getSuperMetaClass() {
            return (MetaClass)this.supperMetaClass;
        }
        
        public Map<String, MetaProperty> getProperties() {
            return this.properties;
        }
        
        public Map<String, MetaAssociation> getAssociations() {
            return this.associations;
        }
        
        public Map<String, MetaScalar> getScalars() {
            return this.scalars;
        }
        
        void secondaryPass(Map<String, MetaClass> metaClasses) {
            TypeMirror superType = (TypeMirror)this.supperMetaClass;
            this.supperMetaClass = null;
            while (superType instanceof DeclaredType) {
                MetaClass superMetaClass = metaClasses.get(superType.toString());
                if (superMetaClass != null) {
                    this.supperMetaClass = superMetaClass;
                    break;
                }
                superType = ((TypeElement)((DeclaredType)superType).asElement()).getSuperclass();
            }
            for (MetaAssociation association : this.associations.values()) {
                association.secondaryPass(metaClasses);
            }
            for (MetaScalar scalar : this.scalars.values()) {
                scalar.secondaryPass(metaClasses);
            }
        }
        
        private AccessType getDefaultAccessType(TypeElement typeElement) {
            AccessType accessType = getAnnotationEnumValue(typeElement, Access.class, "value", AccessType.class);
            if (accessType != null) {
                return accessType;
            }
            for (Element element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                if (containsAnyAnnotation(element, Id.class)) {
                    return AccessType.FIELD;
                }
            }
            for (Element element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                if (containsAnyAnnotation(element, Id.class)) {
                    return AccessType.FIELD;
                }
            }
            TypeMirror superTypeMirror = typeElement.getSuperclass();
            if (superTypeMirror instanceof DeclaredType) {
                TypeElement superElement = ((TypeElement)((DeclaredType)superTypeMirror).asElement());
                if (!superElement.getQualifiedName().equals(Object.class.getName())) {
                    return getDefaultAccessType(superElement);
                }
            }
            return AccessType.PROPERTY;
        }
    }
    
    private static abstract class MetaProperty {
        
        protected MetaClass owner;
        
        protected String name;
        
        static MetaProperty of(MetaClass owner, Element element, Class<? extends Annotation> anyAnnotationType) {
            if (element.getModifiers().contains(Modifier.STATIC)) {
                return null;
            }
            if (containsAnyAnnotation(element, Transient.class)) {
                return null;
            }
            AccessType forceAccessType = getAnnotationEnumValue(element, Access.class, "value", AccessType.class);
            if (element.getKind() == ElementKind.FIELD) {
                if (owner.getAccessType() != AccessType.FIELD &&
                        forceAccessType != AccessType.FIELD) {
                    return null;
                }
            } else if (element.getKind() == ElementKind.METHOD) {
                if (owner.getAccessType() != AccessType.PROPERTY &&
                        forceAccessType != AccessType.PROPERTY) {
                    return null;
                }
            } else {
                return null;
            }
            if (containsAnyAnnotation(
                    element, 
                    OneToOne.class, 
                    ManyToOne.class, 
                    OneToMany.class, 
                    ManyToMany.class,
                    anyAnnotationType)) {
                return new MetaAssociation(owner, element, anyAnnotationType);
            }
            if (element instanceof VariableElement) {
                return new MetaScalar(owner, element);
            } else {
                String methodName = element.getSimpleName().toString();
                if (methodName.startsWith("get") || methodName.startsWith("is")) {
                    return new MetaScalar(owner, element);
                }
            }
            return null;
        }
        
        protected MetaProperty(MetaClass owner, Element element) {
            this.owner = owner;
            String name = element.getSimpleName().toString();
            if (!(element instanceof VariableElement)) {
                name = element.getSimpleName().toString();
                if (name.startsWith("get")) {
                    if (name.length() == 3) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().methodNameIsInvalid(owner.name, "get")
                        );
                    }
                    name = name.substring(3);
                } else if (name.startsWith("is")) {
                    if (name.length() == 2) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().methodNameIsInvalid(owner.name, "is")
                        );
                    }
                    name = name.substring(2);
                } else {
                    throw new IllegalProgramException();
                }
                name = Strings.toCamelCase(name);
            }
            if (name.equals("end")) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().propertyNameIsNotAllowed(
                                owner.name, 
                                "end",
                                TypedQueryPathProcessor.class
                        )
                );
            }
            if (name.equals("asc")) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().propertyNameIsNotAllowed(
                                owner.name, 
                                "asc",
                                TypedQueryPathProcessor.class
                        )
                );
            }
            if (name.equals("desc")) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().propertyNameIsNotAllowed(
                                owner.name, 
                                "desc",
                                TypedQueryPathProcessor.class
                        )
                );
            }
            this.name = name;
        }
        
        public MetaClass getOwner() {
            return this.owner;
        }

        public String getName() {
            return this.name;
        }
    }
    
    private static class MetaAssociation extends MetaProperty {
        
        private boolean collection;
        
        private Object relatedMetaClass;
        
        private MetaAssociation(MetaClass owner, Element element, Class<? extends Annotation> anyAnnotationType) {
            super(owner, element);
            
            this.collection = containsAnyAnnotation(element, OneToMany.class, ManyToMany.class);
            
            Class<?> targetEntity = null;
            if (containsAnyAnnotation(element, OneToOne.class)) {
                targetEntity = (Class<?>)getAnnotationValue(element, OneToOne.class, "targetEntity");
            } else if (containsAnyAnnotation(element, ManyToOne.class)) {
                targetEntity = (Class<?>)getAnnotationValue(element, ManyToOne.class, "targetEntity");
            } else if (containsAnyAnnotation(element, OneToMany.class)) {
                targetEntity = (Class<?>)getAnnotationValue(element, OneToMany.class, "targetEntity");
            } else if (containsAnyAnnotation(element, ManyToMany.class)) {
                targetEntity = (Class<?>)getAnnotationValue(element, ManyToMany.class, "targetEntity");
            } else if (containsAnyAnnotation(element, anyAnnotationType)) {
                targetEntity = (Class<?>)getAnnotationValue(element, anyAnnotationType, "targetEntity");
            }
            if (targetEntity == null || targetEntity == void.class) {
                DeclaredType declaredType;
                if (element instanceof ExecutableElement) {
                    declaredType = (DeclaredType)((ExecutableElement)element).getReturnType();
                } else {
                    declaredType = (DeclaredType)(element).asType();
                }
                if (this.collection) {
                    List<? extends TypeMirror> typeArguments = 
                            declaredType.getTypeArguments();
                    if (typeArguments.isEmpty()) {
                        String message = 
                                "The association \"" +
                                this.name +
                                "\" of \"" +
                                this.getOwner().getName() +
                                "\" is invalid, it has neither targetEntity of \"" +
                                "@" +
                                (containsAnyAnnotation(element, OneToMany.class) ? 
                                OneToMany.class.getName() :
                                ManyToMany.class.getName()) +
                                "\" nor type arguments.";
                                LOGGER.error(message);
                        throw new IllegalArgumentException(message);
                    }
                    this.relatedMetaClass = typeArguments.get(typeArguments.size() - 1).toString();
                } else {
                    this.relatedMetaClass = declaredType.toString();
                }
            } else {
                this.relatedMetaClass = targetEntity.getName();
            }
        }

        public boolean isCollection() {
            return this.collection;
        }
        
        public MetaClass getRelatedMetaClass() {
            return (MetaClass)this.relatedMetaClass;
        }
        
        void secondaryPass(Map<String, MetaClass> metaClasses) {
            String oppositeEndpointName = (String)this.relatedMetaClass;
            this.relatedMetaClass = metaClasses.get(oppositeEndpointName);
            if (this.relatedMetaClass == null) {
                throw new IllegalArgumentException(
                        "Failed to resolve association property " +
                        this.owner.getName() +
                        '.' +
                        this.name +
                        ", because there is no opposite entity: " +
                        oppositeEndpointName);
            }
        }
    }
    
    private static class MetaScalar extends MetaProperty {

        private boolean embedded;
        
        private boolean lazy;
        
        private Object relatedMetaClass;
        
        protected MetaScalar(MetaClass owner, Element element) {
            super(owner, element);
            this.embedded = containsAnyAnnotation(element, Embedded.class);
            this.lazy = FetchType.LAZY.name().equals(getAnnotationValue(element, Basic.class, "fetch"));
            if (this.embedded) {
                if (element instanceof ExecutableElement) {
                    this.relatedMetaClass = ((DeclaredType)((ExecutableElement)element).getReturnType()).toString();
                } else {
                    this.relatedMetaClass = ((DeclaredType)(element).asType()).toString();
                }
            }
        }

        public boolean isEmbedded() {
            return embedded;
        }

        public boolean isLazy() {
            return lazy;
        }
        
        public MetaClass getRelatedMetaClass() {
            return (MetaClass)relatedMetaClass;
        }

        void secondaryPass(Map<String, MetaClass> metaClasses) {
            String embeddedName = (String)this.relatedMetaClass;
            if (embeddedName != null) {
                this.relatedMetaClass = metaClasses.get(embeddedName);
                if (this.relatedMetaClass == null) {
                    throw new IllegalArgumentException(
                            "Failed to resolve the embedded property " +
                            this.owner.getName() +
                            '.' +
                            this.name +
                            ", because there is no opposite emembeddable type: " +
                            embeddedName);
                }
            }
        }
    }
    
    private static class SimpleAnnotationVisitor implements AnnotationValueVisitor<Object, Void> {

        @Override
        public Object visit(AnnotationValue av, Void p) {
            return null;
        }

        @Override
        public Object visit(AnnotationValue av) {
            return null;
        }

        @Override
        public Object visitBoolean(boolean b, Void p) {
            return b;
        }

        @Override
        public Object visitByte(byte b, Void p) {
            return b;
        }

        @Override
        public Object visitChar(char c, Void p) {
            return c;
        }

        @Override
        public Object visitDouble(double d, Void p) {
            return d;
        }

        @Override
        public Object visitFloat(float f, Void p) {
            return f;
        }

        @Override
        public Object visitInt(int i, Void p) {
            return i;
        }

        @Override
        public Object visitLong(long i, Void p) {
            return i;
        }

        @Override
        public Object visitShort(short s, Void p) {
            return s;
        }

        @Override
        public Object visitString(String s, Void p) {
            return s;
        }

        @Override
        public Object visitType(TypeMirror t, Void p) {
            return null;
        }

        @Override
        public Object visitEnumConstant(VariableElement c, Void p) {
            return c.getSimpleName().toString();
        }

        @Override
        public Object visitAnnotation(AnnotationMirror a, Void p) {
            return null;
        }

        @Override
        public Object visitArray(List<? extends AnnotationValue> vals, Void p) {
            Object[] arr = new Object[vals.size()];
            int index = 0;
            for (AnnotationValue annotationValue : vals) {
                arr[index++] = annotationValue.accept(this, null);
            }
            return arr;
        }

        @Override
        public Object visitUnknown(AnnotationValue av, Void p) {
            return null;
        }
    }
    
    private interface Resource {
        
        String methodNameIsInvalid(
                String entityClass,
                String propertyName);
        
        String propertyNameIsNotAllowed(
                String entityClass,
                String propertyName,
                Class<TypedQueryPathProcessor> thisType);
    }
    
    static {
        Class<?> defaultAnyAnnotationType = null;
        String className = "org.hibernate.annotations.Any";
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            if (cl != null) {
                defaultAnyAnnotationType = cl.loadClass(className);
            }
        } catch (ClassNotFoundException ex) {
            // Ignore
        }
        if (defaultAnyAnnotationType == null) {
            try {
                defaultAnyAnnotationType = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                // Ignore
            }
        }
        DEFAULT_ANY_ANNOTATION_TYPE = defaultAnyAnnotationType;
    }
}
