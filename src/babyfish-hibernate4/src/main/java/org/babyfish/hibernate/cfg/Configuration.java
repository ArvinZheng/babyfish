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
package org.babyfish.hibernate.cfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;

import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.association.EntityPropertyAccessor;
import org.babyfish.hibernate.collection.type.AbstractMACollectionType;
import org.babyfish.hibernate.collection.type.MACollectionProperties;
import org.babyfish.hibernate.collection.type.MAListType;
import org.babyfish.hibernate.collection.type.MANavigableMapType;
import org.babyfish.hibernate.collection.type.MANavigableSetType;
import org.babyfish.hibernate.collection.type.MAOrderedMapType;
import org.babyfish.hibernate.collection.type.MAOrderedSetType;
import org.babyfish.hibernate.dialect.InstallableDialect;
import org.babyfish.hibernate.event.ObjectModelMergeEventListener;
import org.babyfish.hibernate.hql.XQueryPlanCache;
import org.babyfish.hibernate.internal.SessionFactoryImplWrapper;
import org.babyfish.hibernate.model.loader.HibernateObjectModelScalarLoader;
import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.hibernate.model.metadata.HibernateObjectModelMetadata;
import org.babyfish.hibernate.model.spi.HibernateObjectModelFactoryProvider;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.UncheckedException;
import org.babyfish.lang.reflect.PropertyInfo;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.spi.ObjectModelFactoryProvider;
import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAProperty;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfish.util.LazyResource;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.bytecode.instrumentation.internal.FieldInterceptionHelper;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Value;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.internal.AbstractServiceRegistryImpl;
import org.hibernate.service.spi.ServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.tuple.NonIdentifierAttribute;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.BasicType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.BigIntegerType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.SerializationException;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author Tao Chen
 */
public class Configuration extends org.hibernate.cfg.Configuration {

    private static final long serialVersionUID = 7942896335754607308L;
    
    private static boolean pathPlanKeyVlidationSuspended;
    
    private static final Logger sessionFactoryImplLog = 
            LoggerFactory.getLogger(org.hibernate.internal.SessionFactoryImpl.class);
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final Method ABSTACT_SERVICE_RESGISTRY_IMPL_CREATE_SERVICE_BINDING;
    
    private static final Field QUERY_PLAN_CACHE_FIELD;
    
    private static final Method CHECK_NAMED_QUERIES_METHOD;
    
    private static final String INSTRUMENT_EXPECTED_POM_SECTION =
            "<plugin>\n" +
            "  <artifactId>maven-antrun-plugin</artifactId>\n" +
            "  <executions>\n" +
            "    <execution>\n" +
            "      <phase>process-test-classes</phase>\n" +
            "      <goals>\n" +
            "        <goal>run</goal>\n" +
            "      </goals>\n" +
            "    </execution>\n" +
            "  </executions>\n" +
            "  <dependencies>\n" +
            "    <dependency>\n" +
            "      <groupId>org.babyfish</groupId>\n" +
            "      <artifactId>babyfish-hibernate-tool</artifactId>\n" +
            "      <version>${babyfish.version}</version>\n" +
            "    </dependency>\n" +
            "  </dependencies>\n" +
            "  <configuration>\n" +
            "    <tasks>\n" +
            "      <taskdef name=\"instrument\" classname=\"org.babyfish.hibernate.tool.InstrumentTask\">\n" +
            "        <classpath>\n" +
            "          <path refid=\"maven.runtime.classpath\" />\n" +
            "          <path refid=\"maven.plugin.classpath\" />\n" +
            "        </classpath>\n" +
            "      </taskdef>\n" +
            "      <instrument>\n" +
            "        <fileset dir=\"${project.build.outputDirectory}\">\n" +
            "          <include name=\"**/entities/*.class\" />\n" +
            "        </fileset>\n" +
            "      </instrument>\n" +
            "    </tasks>\n" +
            "  </configuration>\n" +
            "</plugin>";
    
    public Configuration() {
        super(new org.babyfish.hibernate.cfg.SettingsFactory());
        this.registerXPojoEntityTuplizer();
        this.addNativeFunctions();
    }

    protected Configuration(org.babyfish.hibernate.cfg.SettingsFactory settingsFactory) {
        super(settingsFactory);
        this.registerXPojoEntityTuplizer();
        this.addNativeFunctions();
    }

    @Override
    public Configuration configure() throws HibernateException {
        super.configure();
        return this;
    }

    @Override
    public Configuration configure(String resource) throws HibernateException {
        super.configure(resource);
        return this;
    }

    @Override
    public Configuration configure(URL url) throws HibernateException {
        super.configure(url);
        return this;
    }

    @Override
    public Configuration configure(File configFile) throws HibernateException {
        super.configure(configFile);
        return this;
    }

    @Override
    public Configuration configure(Document document) throws HibernateException {
        super.configure(document);
        return this;
    }

    @Override
    public Configuration addFile(String xmlFile) throws MappingException {
        super.addFile(xmlFile);
        return this;
    }

    @Override
    public Configuration addFile(File xmlFile) throws MappingException {
        super.addFile(xmlFile);
        return this;
    }

    @Override
    public Configuration addCacheableFile(File xmlFile) throws MappingException {
        super.addCacheableFile(xmlFile);
        return this;
    }

    @Override
    public Configuration addCacheableFileStrictly(File xmlFile)
            throws SerializationException, FileNotFoundException {
        super.addCacheableFileStrictly(xmlFile);
        return this;
    }

    @Override
    public Configuration addCacheableFile(String xmlFile)
            throws MappingException {
        super.addCacheableFile(xmlFile);
        return this;
    }

    @Override
    public Configuration addXML(String xml) throws MappingException {
        super.addXML(xml);
        return this;
    }

    @Override
    public Configuration addURL(URL url) throws MappingException {
        super.addURL(url);
        return this;
    }

    @Override
    public Configuration addDocument(Document doc) throws MappingException {
        super.addDocument(doc);
        return this;
    }

    @Override
    public Configuration addInputStream(InputStream xmlInputStream)
            throws MappingException {
        super.addInputStream(xmlInputStream);
        return this;
    }

    @Override
    public Configuration addResource(String resourceName,
            ClassLoader classLoader) throws MappingException {
        super.addResource(resourceName, classLoader);
        return this;
    }

    @Override
    public Configuration addResource(String resourceName)
            throws MappingException {
        super.addResource(resourceName);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Configuration addClass(Class persistentClass)
            throws MappingException {
        super.addClass(persistentClass);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Configuration addAnnotatedClass(Class annotatedClass) {
        super.addAnnotatedClass(annotatedClass);
        return this;
    }

    @Override
    public Configuration addPackage(String packageName) throws MappingException {
        super.addPackage(packageName);
        return this;
    }

    @Override
    public Configuration addJar(File jar) throws MappingException {
        super.addJar(jar);
        return this;
    }

    @Override
    public Configuration addDirectory(File dir) throws MappingException {
        super.addDirectory(dir);
        return this;
    }

    @Override
    public Configuration setInterceptor(Interceptor interceptor) {
        super.setInterceptor(interceptor);
        return this;
    }

    @Override
    public Configuration setProperties(Properties properties) {
        super.setProperties(properties);
        return this;
    }

    @Override
    public Configuration addProperties(Properties extraProperties) {
        super.addProperties(extraProperties);
        return this;
    }

    @Override
    public Configuration setProperty(String propertyName, String value) {
        super.setProperty(propertyName, value);
        return this;
    }

    @Override
    public Configuration setCacheConcurrencyStrategy(String entityName,
            String concurrencyStrategy) {
        super.setCacheConcurrencyStrategy(entityName, concurrencyStrategy);
        return this;
    }

    @Override
    public Configuration setCacheConcurrencyStrategy(
            String entityName,
            String concurrencyStrategy, String region) {
        super.setCacheConcurrencyStrategy(entityName, concurrencyStrategy, region);
        return this;
    }

    @Override
    public Configuration setCollectionCacheConcurrencyStrategy(
            String collectionRole, String concurrencyStrategy) {
        super.setCollectionCacheConcurrencyStrategy(collectionRole,
                concurrencyStrategy);
        return this;
    }

    @Override
    public Configuration setNamingStrategy(NamingStrategy namingStrategy) {
        super.setNamingStrategy(namingStrategy);
        return this;
    }
    
    protected XQueryPlanCache createQueryPlanCache(SessionFactoryImplementor factory) {
        return new XQueryPlanCache(factory);
    }

    @Override
    protected void secondPassCompile() throws MappingException {
        super.secondPassCompile();
        replaceBasicTypesJavaTypeDescriptor();
        processPersistentClasses();
    }

    @Deprecated
    @Override
    public XSessionFactory buildSessionFactory() throws HibernateException {
        return (XSessionFactory)super.buildSessionFactory();
    }

    @Override
    public XSessionFactory buildSessionFactory(ServiceRegistry serviceRegistry) throws HibernateException {
        Arguments.mustBeInstanceOfValue(
                "serviceRegistry", 
                Arguments.mustNotBeNull("serviceRegistry", serviceRegistry),
                StandardServiceRegistryImpl.class
        );
        replacePersisterClassResolver((AbstractServiceRegistryImpl)serviceRegistry);
        
        String originalCurrentSessionContext = this.getProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS);
        this.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        SessionFactoryImpl factory;
        try {
            pathPlanKeyVlidationSuspended = true;
            try {
                factory = (SessionFactoryImpl)super.buildSessionFactory(serviceRegistry);
            } finally {
                pathPlanKeyVlidationSuspended = false;
            }
        } finally {
            if (originalCurrentSessionContext != null) {
                this.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, originalCurrentSessionContext);
            } else {
                this.getProperties().remove(Environment.CURRENT_SESSION_CONTEXT_CLASS);
            }
        }
        if (originalCurrentSessionContext != null) {
            factory.getProperties().setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, originalCurrentSessionContext);
        } else {
            factory.getProperties().remove(Environment.CURRENT_SESSION_CONTEXT_CLASS);
        }
        
        Dialect dialect = factory.getDialect();
        if (dialect instanceof InstallableDialect) {
            ((InstallableDialect)dialect).install(factory);
        }
        
        EventListenerGroup<MergeEventListener> mergeEventListenerGroup =
                factory
                .getServiceRegistry()
                .getService(EventListenerRegistry.class)
                .getEventListenerGroup(EventType.MERGE);
        MergeEventListener mergeEventListener = new ObjectModelMergeEventListener(mergeEventListenerGroup.listeners());
        mergeEventListenerGroup.clear();
        mergeEventListenerGroup.appendListener(mergeEventListener);
        
        setQueryPlanceCache(factory, this.createQueryPlanCache(factory));
        
        for (ClassMetadata classMetadata : factory.getAllClassMetadata().values()) {
            if(Metadatas.getObjectModelFactoryProvider(classMetadata.getMappedClass()) != null) {
            //Validate whether JPA configuration is same with object model configuration
                HibernateMetadatas.of(classMetadata.getMappedClass()).getPersistentClass(factory);
            }
        }
        
        return SessionFactoryImplWrapper.wrap(factory);
    }
    
    public static boolean isPathPlanKeyVlidationSuspended() {
        return pathPlanKeyVlidationSuspended;
    }
    
    private static void replacePersisterClassResolver(AbstractServiceRegistryImpl abstractServiceRegistryImpl) {
        try {
            ABSTACT_SERVICE_RESGISTRY_IMPL_CREATE_SERVICE_BINDING.invoke(
                    abstractServiceRegistryImpl, 
                    new StandardServiceInitiator<PersisterClassResolver>() {
                        @Override
                        public Class<PersisterClassResolver> getServiceInitiated() {
                            return PersisterClassResolver.class;
                        }
                        @SuppressWarnings("rawtypes")
                        @Override
                        public PersisterClassResolver initiateService(
                                Map configurationValues,
                                ServiceRegistryImplementor registry) {
                            return new org.babyfish.hibernate.persister.StandardPersisterClassResolver();
                        }
                    }
            );
            ABSTACT_SERVICE_RESGISTRY_IMPL_CREATE_SERVICE_BINDING.invoke(
                    abstractServiceRegistryImpl, 
                    new StandardServiceInitiator<PersisterFactory>() {
                        @Override
                        public Class<PersisterFactory> getServiceInitiated() {
                            return PersisterFactory.class;
                        }
                        @SuppressWarnings("rawtypes")
                        @Override
                        public PersisterFactory initiateService(
                                Map configurationValues,
                                ServiceRegistryImplementor registry) {
                            return new org.hibernate.persister.internal.PersisterFactoryImpl();
                        }
                    }
            );
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void replaceBasicTypesJavaTypeDescriptor() {
        TypeResolver typeResolver = this.getTypeResolver();
        BasicTypeRegistry basicTypeRegistry;
        try {
            Field field = typeResolver.getClass().getDeclaredField("basicTypeRegistry");
            field.setAccessible(true);
            basicTypeRegistry = (BasicTypeRegistry)field.get(typeResolver);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
        
        Map<String,BasicType> registry;
        try {
            Field field = basicTypeRegistry.getClass().getDeclaredField("registry");
            field.setAccessible(true);
            registry = (Map<String, BasicType>)field.get(basicTypeRegistry);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
        
        for (BasicType basicType : registry.values()) {
            if (basicType instanceof AbstractStandardBasicType<?>) {
                WrapperJavaTypeDescriptor.replaceJavaTypeDescriptor((AbstractStandardBasicType<?>)basicType);
            }
        }
    }
    
    private void processPersistentClasses() {
        Iterator<PersistentClass> classMappings = this.getClassMappings();
        while (classMappings.hasNext()) { //TODO: please iterate its subclasses.
            PersistentClass persistentClass = classMappings.next();
            Class<?> mappedClass = persistentClass.getMappedClass();
            ObjectModelFactoryProvider provider = Metadatas.getObjectModelFactoryProvider(mappedClass);
            if (provider == null) {
                if (mappedClass.isAnnotationPresent(JPAObjectModelInstrument.class)) {
                    throw new IllegalProgramException(LAZY_RESOURCE.get().missInstrument(
                            mappedClass, 
                            JPAObjectModelInstrument.class,
                            INSTRUMENT_EXPECTED_POM_SECTION)
                    );
                }
            } else {
                if (!(provider instanceof HibernateObjectModelFactoryProvider)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().requiredHibernateObjectModelFactoryProvider(mappedClass, HibernateObjectModelFactoryProvider.class)
                    );
                }
                HibernateObjectModelMetadata metadata = HibernateMetadatas.of(mappedClass);
                for (org.babyfish.model.metadata.Property property : metadata.getDeclaredProperties().values()) {
                    JPAProperty jpaProperty = (JPAProperty)property;
                    if (jpaProperty instanceof AssociationProperty) {
                        AssociationProperty associationProperty = (AssociationProperty)jpaProperty;
                        if (associationProperty.getCovarianceProperty() != null) {
                            continue;
                        }
                    }
                    PropertyInfo ownerProperty = jpaProperty.getOwnerProperty();
                    if (ownerProperty == null) {
                        continue;
                    }
                    Property mappingProperty = persistentClass.getProperty(ownerProperty.getName());
                    mappingProperty.setPropertyAccessorName(EntityPropertyAccessor.class.getName());
                    Value value = mappingProperty.getValue();
                    if (property instanceof AssociationProperty) {
                        JPAAssociationProperty jpaAssociationProperty = (JPAAssociationProperty)property;
                        Class<?> standardReturnType = jpaAssociationProperty.getStandardReturnClass();
                        /*
                         * (1) Don't use jpaAssocationProperty.getHibernateProperty().
                         * This is org.hiberante.mapping.Property, that is org.hibernate.tuple.Property
                         * 
                         * (2) Don't invoke property.getType() or property.getValue().getType()
                         * that will cause the creating of original collection-type before the replacement.
                         */
                        if (jpaAssociationProperty.getCovarianceProperty() == null) {
                            if (standardReturnType == NavigableMap.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Map.class, 
                                        MANavigableMapType.class);
                            } else if (standardReturnType == XOrderedMap.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Map.class, 
                                        MAOrderedMapType.class);
                            } else if (standardReturnType == Map.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Map.class, 
                                        MAOrderedMapType.class);
                            } else if (standardReturnType == NavigableSet.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Set.class, 
                                        MANavigableSetType.class);
                            } else if (standardReturnType == XOrderedSet.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Set.class, 
                                        MAOrderedSetType.class);
                            } else if (standardReturnType == Set.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Set.class, 
                                        MAOrderedSetType.class);
                            } else if (standardReturnType == List.class) {
                                if (org.hibernate.mapping.Bag.class.isAssignableFrom(mappingProperty.getValue().getClass())) {
                                    throw new MappingException("In ObjectModel4ORM, Bag proeprty must be declared as java.util.Collection(not java.util.List)");
                                }
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.List.class, 
                                        MAListType.class);
                            } if (standardReturnType == Collection.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Bag.class, 
                                        MAOrderedSetType.class);
                            }
                            if (value instanceof org.hibernate.mapping.Collection) {
                                org.hibernate.mapping.Collection collection = 
                                    (org.hibernate.mapping.Collection)value;
                                collection.setTypeParameters(
                                        new MACollectionProperties(
                                                jpaAssociationProperty, 
                                                collection.getTypeParameters()));
                            }
                            
                            if (jpaAssociationProperty.getOwnerIndexProperty() != null) {
                                persistentClass
                                .getProperty(jpaAssociationProperty.getOwnerIndexProperty().getName())
                                .setPropertyAccessorName(EntityPropertyAccessor.class.getName());
                            }
                            if (jpaAssociationProperty.getOwnerKeyProperty() != null) {
                                persistentClass
                                .getProperty(jpaAssociationProperty.getOwnerKeyProperty().getName())
                                .setPropertyAccessorName(EntityPropertyAccessor.class.getName());
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void registerXPojoEntityTuplizer() {
        this.getEntityTuplizerFactory().registerDefaultTuplizerClass(
                EntityMode.POJO, 
                XPojoEntityTuplizer.class);
    }
    
    @SuppressWarnings("unchecked")
    private void addNativeFunctions() {
        Map<String, SQLFunction> map = this.getSqlFunctions();
        
        map.put(
                "nativeInteger", 
                new AbstractNativeFunction() {
                    @Override
                    public Type getReturnType(Type firstArgumentType, Mapping mapping) 
                            throws QueryException {
                        return IntegerType.INSTANCE;
                    }
                });
        map.put(
                "nativeLong", 
                new AbstractNativeFunction() {
                    @Override
                    public Type getReturnType(Type firstArgumentType, Mapping mapping) 
                            throws QueryException {
                        return LongType.INSTANCE;
                    }
                });
        map.put(
                "nativeFloat", 
                new AbstractNativeFunction() {
                    @Override
                    public Type getReturnType(Type firstArgumentType, Mapping mapping) 
                            throws QueryException {
                        return FloatType.INSTANCE;
                    }
                });
        map.put(
                "nativeDouble", 
                new AbstractNativeFunction() {
                    @Override
                    public Type getReturnType(Type firstArgumentType, Mapping mapping) 
                            throws QueryException {
                        return DoubleType.INSTANCE;
                    }
                });
        map.put(
                "nativeBigInteger", 
                new AbstractNativeFunction() {
                    @Override
                    public Type getReturnType(Type firstArgumentType, Mapping mapping) 
                            throws QueryException {
                        return BigIntegerType.INSTANCE;
                    }
                });
        map.put(
                "nativeBigDecimal", 
                new AbstractNativeFunction() {
                    @Override
                    public Type getReturnType(Type firstArgumentType, Mapping mapping) 
                            throws QueryException {
                        return BigDecimalType.INSTANCE;
                    }
                });
        map.put(
                "nativeString", 
                new AbstractNativeFunction() {
                    @Override
                    public Type getReturnType(Type firstArgumentType, Mapping mapping) 
                            throws QueryException {
                        return StringType.INSTANCE;
                    }
                });
        //TODO: more types, all the derived types of org.hibernate.type.AbstractSingleColumnStandardBasicType
    }

    private static void replaceUserCollectionType(
            Property mappingProperty,
            Class<? extends org.hibernate.mapping.Collection> hibernateCollectionType,
            Class<? extends AbstractMACollectionType> babyfishCollectionType) {
        /*
         * Don't invoke property.getType() or property.getValue().getType()
         * that will cause the creating of original collection-type before the replacement.
         * that is is slow
         */
        Value value = mappingProperty.getValue();
        if (!(value instanceof org.hibernate.mapping.Collection)) {
            throw new MappingException(
                    '"' +
                    mappingProperty.getPersistentClass().getEntityName() +
                    '.' +
                    mappingProperty.getName() +
                    "\" must be mapped as collection.");
        }
        org.hibernate.mapping.Collection collection = (org.hibernate.mapping.Collection)value;
        String typeName = collection.getTypeName();
        if (typeName == null) {
            if (!hibernateCollectionType.isAssignableFrom(value.getClass())) {
                throw new MappingException(
                        '"' +
                        mappingProperty.getPersistentClass().getEntityName() +
                        '.' +
                        mappingProperty.getName() +
                        "\" must be mapped collection whose hibernate type is \"" +
                        hibernateCollectionType.getName() +
                        "\".");
            }
            collection.setTypeName(babyfishCollectionType.getName());
        } else {
            Class<?> userCollctionType;
            try {
                userCollctionType = ReflectHelper.classForName(typeName);
            } catch (ClassNotFoundException ex) {
                throw new MappingException(
                        '"' +
                        mappingProperty.getPersistentClass().getEntityName() +
                        '.' +
                        mappingProperty.getName() +
                        "\" must be mapped as collection whose attribute \"collection-type\" is \"" +
                        typeName +
                        "\", but the there is no java type names\"" +
                        typeName +
                        "\".");
            }
            if (!babyfishCollectionType.isAssignableFrom(userCollctionType)) {
                throw new MappingException(
                        '"' +
                        mappingProperty.getPersistentClass().getEntityName() +
                        '.' +
                        mappingProperty.getName() +
                        "\" must be mapped as collection whose attribut \"collection-type\" is \"" +
                        typeName +
                        "\", but the there class \"" +
                        typeName +
                        "\" is not \"" +
                        babyfishCollectionType.getName() +
                        "\" or its derived class.");
            }
        }
    }
    
    // This static internal method will be invoked by the bytec-ode generated by me runtime
    static void setQueryPlanceCache(SessionFactoryImpl factory, QueryPlanCache queryPlanCache) {
        try {
            QUERY_PLAN_CACHE_FIELD.set(factory, queryPlanCache);
        } catch (IllegalAccessException ex) {
            throw new AssertionError();
        }
    }
    
    //This static internal method will be invoked by the bytec-ode generated by me runtime
    @SuppressWarnings("unchecked")
    static void checkNamedQueries(SessionFactoryImpl factory) {
        Settings settings = factory.getSettings();
        if (settings.isNamedQueryStartupCheckingEnabled()) {
            Map<String, HibernateException> errors;
            try {
                errors = (Map<String, HibernateException>)CHECK_NAMED_QUERIES_METHOD.invoke(factory);
            } catch (IllegalAccessException ex) {
                throw new AssertionError();
            } catch (InvocationTargetException ex) {
                throw UncheckedException.rethrow(ex.getTargetException());
            }
            if (!errors.isEmpty()) {
                boolean addComma = false;
                StringBuilder builder = new StringBuilder( "Errors in named queries: " );
                for (Entry<String, HibernateException> entry : errors.entrySet()) {
                    String queryName = entry.getKey();
                    HibernateException e = entry.getValue();
                    if (addComma) {
                        builder.append(", ");
                    } else {
                        addComma = true;
                    }
                    builder.append(queryName);
                    sessionFactoryImplLog.error("Error in named query: " + queryName, e);
                }
                throw new HibernateException( builder.toString() );
            }
        }
    }
    
    private static class XPojoEntityTuplizer extends PojoEntityTuplizer {

        public XPojoEntityTuplizer(EntityMetamodel entityMetamodel, EntityBinding mappedEntity) {
            super(entityMetamodel, mappedEntity);
        }

        public XPojoEntityTuplizer(EntityMetamodel entityMetamodel, PersistentClass mappedEntity) {
            super(entityMetamodel, mappedEntity);
        }

        @Override
        public Object[] getPropertyValues(Object entity) throws HibernateException {
            Object[] arr = super.getPropertyValues(entity);
            FieldInterceptor fieldInterceptor = FieldInterceptionHelper.extractFieldInterceptor(entity);
            if (fieldInterceptor instanceof HibernateObjectModelScalarLoader) {
                HibernateObjectModelScalarLoader hibernateObjectModelScalarLoader =
                        (HibernateObjectModelScalarLoader)fieldInterceptor;
                ObjectModel objectModel = hibernateObjectModelScalarLoader.getObjectModel();
                JPAObjectModelMetadata objectModelMetadata = objectModel.getObjectModelMetadata();
                NonIdentifierAttribute[] attributes = this.getEntityMetamodel().getProperties();
                for (int i = attributes.length - 1; i >= 0; i--) {
                    if (arr[i] == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
                        JPAProperty property = objectModelMetadata.getMappingSources().get(attributes[i].getName());
                        if (property instanceof JPAScalarProperty && !objectModel.isUnloaded(property.getId())) {
                            arr[i] = objectModel.getScalar(property.getId());
                        }
                    }
                }
            }
            return arr;
        }
    }
    
    private static class WrapperJavaTypeDescriptor implements JavaTypeDescriptor<Object> {
        
        private static final long serialVersionUID = -8627363999213768175L;
        
        private JavaTypeDescriptor<Object> raw;

        @SuppressWarnings("unchecked")
        public WrapperJavaTypeDescriptor(JavaTypeDescriptor<?> raw) {
            super();
            this.raw = (JavaTypeDescriptor<Object>)raw;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        static void replaceJavaTypeDescriptor(AbstractStandardBasicType<?> abstractStandardBasicType) {
            JavaTypeDescriptor<?> javaTypeDescriptor = abstractStandardBasicType.getJavaTypeDescriptor();
            if (javaTypeDescriptor != null && !(javaTypeDescriptor instanceof WrapperJavaTypeDescriptor)) {
                abstractStandardBasicType.setJavaTypeDescriptor((JavaTypeDescriptor)new WrapperJavaTypeDescriptor(javaTypeDescriptor));
            }
        }

        @Override
        public boolean areEqual(Object one, Object another) {
            // Fix the bugs of Hibernate!
            //
            // Hibernate can NOT guarantee that the parameters of this method
            // can't be LazyPropertyInitializer.UNFETCHED_PROPERTY absolutely,
            // so I intercept the JavaTypeDescriptor and add the logic here
            // though it does not looks like a good idea.
            //
            // One of these bugs is reported by me: 
            // Please see "https://hibernate.atlassian.net/browse/HHH-9379"
            if (one == another) {
                return true;
            }
            if (one == LazyPropertyInitializer.UNFETCHED_PROPERTY || 
                    another == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
                return false;
            }
            return raw.areEqual(one, another);
        }

        @Override
        public Class<Object> getJavaTypeClass() {
            return raw.getJavaTypeClass();
        }

        @Override
        public MutabilityPlan<Object> getMutabilityPlan() {
            return raw.getMutabilityPlan();
        }

        @Override
        public Comparator<Object> getComparator() {
            return raw.getComparator();
        }

        @Override
        public int extractHashCode(Object value) {
            return raw.extractHashCode(value);
        }

        @Override
        public String extractLoggableRepresentation(Object value) {
            return raw.extractLoggableRepresentation(value);
        }

        @Override
        public String toString(Object value) {
            return raw.toString(value);
        }

        @Override
        public Object fromString(String string) {
            return raw.fromString(string);
        }

        @Override
        public <X> X unwrap(Object value, Class<X> type, WrapperOptions options) {
            return raw.unwrap(value, type, options);
        }

        @Override
        public <X> Object wrap(X value, WrapperOptions options) {
            return raw.wrap(value, options);
        }
    }
    
    private static abstract class AbstractNativeFunction implements SQLFunction {

        @Override
        public boolean hasArguments() {
            return true;
        }

        @Override
        public boolean hasParenthesesIfNoArguments() {
            return true;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public String render(
                Type firstArgumentType, 
                List arguments, 
                SessionFactoryImplementor factory) 
        throws QueryException {
            return "1";
        }
        
    }
    
    private interface Resource {
        
        String requiredHibernateObjectModelFactoryProvider(
                Class<?> entityClass,
                Class<HibernateObjectModelFactoryProvider> hibernateObjectModelFactoryProvider);
        
        String missInstrument(
                Class<?> entityClass,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentConstant,
                String instrumentExpectedPOMSection);
    }
    
    static {
        Method abstractServiceRegistryImplCreateServiceBinding;
        try {
            abstractServiceRegistryImplCreateServiceBinding = 
                    AbstractServiceRegistryImpl.class.getDeclaredMethod(
                            "createServiceBinding", 
                            ServiceInitiator.class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
        abstractServiceRegistryImplCreateServiceBinding.setAccessible(true);
        
        Method checkNamedQueriesMethod;
        try {
            checkNamedQueriesMethod = SessionFactoryImpl.class.getDeclaredMethod("checkNamedQueries");
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
        checkNamedQueriesMethod.setAccessible(true);
        
        Field queryPlanCacheField;
        try {
            queryPlanCacheField = SessionFactoryImpl.class.getDeclaredField("queryPlanCache");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
        queryPlanCacheField.setAccessible(true);
        
        ABSTACT_SERVICE_RESGISTRY_IMPL_CREATE_SERVICE_BINDING = abstractServiceRegistryImplCreateServiceBinding;
        CHECK_NAMED_QUERIES_METHOD = checkNamedQueriesMethod;
        QUERY_PLAN_CACHE_FIELD = queryPlanCacheField;
    }
    
}
