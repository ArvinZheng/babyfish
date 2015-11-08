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
package org.babyfish.hibernate.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;

import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.association.JPAObjectModelPropertyAccessStrategy;
import org.babyfish.hibernate.boot.XMetadata;
import org.babyfish.hibernate.boot.XMetadataImplementor;
import org.babyfish.hibernate.boot.XSessionFactoryBuilder;
import org.babyfish.hibernate.boot.XSessionFactoryBuilderFactory;
import org.babyfish.hibernate.collection.type.AbstractMACollectionType;
import org.babyfish.hibernate.collection.type.MACollectionProperties;
import org.babyfish.hibernate.collection.type.MAListType;
import org.babyfish.hibernate.collection.type.MANavigableMapType;
import org.babyfish.hibernate.collection.type.MANavigableSetType;
import org.babyfish.hibernate.collection.type.MAOrderedMapType;
import org.babyfish.hibernate.collection.type.MAOrderedSetType;
import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.hibernate.model.metadata.HibernateObjectModelMetadata;
import org.babyfish.hibernate.model.spi.HibernateObjectModelFactoryProvider;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.reflect.PropertyInfo;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.spi.ObjectModelFactoryProvider;
import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAProperty;
import org.babyfish.util.LazyResource;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.TypeDefinition;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.cfg.annotations.NamedEntityGraphDefinition;
import org.hibernate.cfg.annotations.NamedProcedureCallDefinition;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Value;
import org.hibernate.type.TypeResolver;

/**
 * @author Tao Chen
 */
public class XMetadataImpl extends MetadataImpl implements XMetadata, XMetadataImplementor {

	private static final long serialVersionUID = -8414435620066350832L;
	
	private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
	
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
    
    @SuppressWarnings("rawtypes")
	public XMetadataImpl(
    		UUID uuid, 
    		MetadataBuildingOptions metadataBuildingOptions, 
    		TypeResolver typeResolver,
			MutableIdentifierGeneratorFactory identifierGeneratorFactory, 
			Map<String, PersistentClass> entityBindingMap,
			Map<Class, MappedSuperclass> mappedSuperclassMap, 
			Map<String, Collection> collectionBindingMap,
			Map<String, TypeDefinition> typeDefinitionMap, 
			Map<String, FilterDefinition> filterDefinitionMap,
			Map<String, FetchProfile> fetchProfileMap, 
			Map<String, String> imports,
			Map<String, IdentifierGeneratorDefinition> idGeneratorDefinitionMap,
			Map<String, NamedQueryDefinition> namedQueryMap, 
			Map<String, NamedSQLQueryDefinition> namedNativeQueryMap,
			Map<String, NamedProcedureCallDefinition> namedProcedureCallMap,
			Map<String, ResultSetMappingDefinition> sqlResultSetMappingMap,
			Map<String, NamedEntityGraphDefinition> namedEntityGraphMap, 
			Map<String, SQLFunction> sqlFunctionMap,
			Database database) {
		super(
				uuid, 
				metadataBuildingOptions, 
				typeResolver, 
				identifierGeneratorFactory, 
				entityBindingMap, 
				mappedSuperclassMap,
				collectionBindingMap, 
				typeDefinitionMap, 
				filterDefinitionMap, 
				fetchProfileMap, 
				imports,
				idGeneratorDefinitionMap, 
				namedQueryMap, 
				namedNativeQueryMap, 
				namedProcedureCallMap, 
				sqlResultSetMappingMap,
				namedEntityGraphMap, 
				sqlFunctionMap, 
				database
		);
		this.processPersistentClasses();
	}
    
    @SuppressWarnings("rawtypes")
	public XMetadataImpl(MetadataImpl metadata) {
    	this(
				metadata.getUUID(), 
				metadata.getMetadataBuildingOptions(), 
				metadata.getTypeResolver(), 
				(MutableIdentifierGeneratorFactory)getObject(metadata, "identifierGeneratorFactory", IdentifierGeneratorFactory.class), 
				XMetadataImpl.<PersistentClass>getStringMap(metadata, "entityBindingMap"), 
				XMetadataImpl.<Class, MappedSuperclass>getMap(metadata, "mappedSuperclassMap"),
			    XMetadataImpl.<Collection>getStringMap(metadata, "collectionBindingMap"), 
			    XMetadataImpl.<TypeDefinition>getStringMap(metadata, "typeDefinitionMap"), 
			    XMetadataImpl.<FilterDefinition>getStringMap(metadata, "filterDefinitionMap"), 
			    XMetadataImpl.<FetchProfile>getStringMap(metadata, "fetchProfileMap"), 
				metadata.getImports(),
				XMetadataImpl.<IdentifierGeneratorDefinition>getStringMap(metadata, "idGeneratorDefinitionMap"), 
			    XMetadataImpl.<NamedQueryDefinition>getStringMap(metadata, "namedQueryMap"), 
			    XMetadataImpl.<NamedSQLQueryDefinition>getStringMap(metadata, "namedNativeQueryMap"), 
			    XMetadataImpl.<NamedProcedureCallDefinition>getStringMap(metadata, "namedProcedureCallMap"), 
			    XMetadataImpl.<ResultSetMappingDefinition>getStringMap(metadata, "sqlResultSetMappingMap"),
			    XMetadataImpl.<NamedEntityGraphDefinition>getStringMap(metadata, "namedEntityGraphMap"), 
			    XMetadataImpl.<SQLFunction>getStringMap(metadata, "sqlFunctionMap"), 
				metadata.getDatabase()
		);
    }

	@Override
	public XSessionFactoryBuilder getSessionFactoryBuilder() {
		final XSessionFactoryBuilderImpl defaultBuilder = new XSessionFactoryBuilderImpl( this );

		final ClassLoaderService cls = 
				this
				.getMetadataBuildingOptions()
				.getServiceRegistry()
				.getService( ClassLoaderService.class );
		final java.util.Collection<SessionFactoryBuilderFactory> discoveredBuilderFactories = 
				cls.loadJavaServices( SessionFactoryBuilderFactory.class );

		XSessionFactoryBuilder builder = null;
		List<String> activeFactoryNames = null;

		for (SessionFactoryBuilderFactory discoveredBuilderFactory : discoveredBuilderFactories) {
			if (!(discoveredBuilderFactory instanceof XSessionFactoryBuilderFactory)) {
				throw new IllegalProgramException();
			}
			final XSessionFactoryBuilder returnedBuilder = 
					((XSessionFactoryBuilderFactory)discoveredBuilderFactory)
					.getSessionFactoryBuilder(this, defaultBuilder);
			if ( returnedBuilder != null ) {
				if ( activeFactoryNames == null ) {
					activeFactoryNames = new ArrayList<String>();
				}
				activeFactoryNames.add(discoveredBuilderFactory.getClass().getName());
				builder = returnedBuilder;
			}
		}

		if (activeFactoryNames != null && activeFactoryNames.size() > 1) {
			throw new HibernateException(
					"Multiple active SessionFactoryBuilderFactory definitions were discovered : " +
							StringHelper.join( ", ", activeFactoryNames )
			);
		}

		if (builder != null) {
			return builder;
		}

		return defaultBuilder;
	}

	@Override
	public XSessionFactory buildSessionFactory() {
		return getSessionFactoryBuilder().build();
	}
	
	private void processPersistentClasses() {
		ClassLoaderService classLoaderService = 
				this
				.getMetadataBuildingOptions()
				.getServiceRegistry()
				.getService(ClassLoaderService.class);
		
		for (PersistentClass persistentClass : this.getEntityBindings()) {
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
                    mappingProperty.setPropertyAccessorName(JPAObjectModelPropertyAccessStrategy.class.getName());
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
                                        MANavigableMapType.class,
                                        classLoaderService);
                            } else if (standardReturnType == XOrderedMap.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Map.class, 
                                        MAOrderedMapType.class,
                                        classLoaderService);
                            } else if (standardReturnType == Map.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Map.class, 
                                        MAOrderedMapType.class,
                                        classLoaderService);
                            } else if (standardReturnType == NavigableSet.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Set.class, 
                                        MANavigableSetType.class,
                                        classLoaderService);
                            } else if (standardReturnType == XOrderedSet.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Set.class, 
                                        MAOrderedSetType.class,
                                        classLoaderService);
                            } else if (standardReturnType == Set.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Set.class, 
                                        MAOrderedSetType.class,
                                        classLoaderService);
                            } else if (standardReturnType == List.class) {
                                if (org.hibernate.mapping.Bag.class.isAssignableFrom(mappingProperty.getValue().getClass())) {
                                    throw new MappingException("In ObjectModel4ORM, Bag proeprty must be declared as java.util.Collection(not java.util.List)");
                                }
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.List.class, 
                                        MAListType.class,
                                        classLoaderService);
                            } if (standardReturnType == Collection.class) {
                                replaceUserCollectionType(
                                        mappingProperty, 
                                        org.hibernate.mapping.Bag.class, 
                                        MAOrderedSetType.class,
                                        classLoaderService);
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
                                .setPropertyAccessorName(JPAObjectModelPropertyAccessStrategy.class.getName());
                            }
                            if (jpaAssociationProperty.getOwnerKeyProperty() != null) {
                                persistentClass
                                .getProperty(jpaAssociationProperty.getOwnerKeyProperty().getName())
                                .setPropertyAccessorName(JPAObjectModelPropertyAccessStrategy.class.getName());
                            }
                        }
                    }
                }
	        }
		}
	}
	
	private static void replaceUserCollectionType(
            Property mappingProperty,
            Class<? extends org.hibernate.mapping.Collection> hibernateCollectionType,
            Class<? extends AbstractMACollectionType> babyfishCollectionType,
            ClassLoaderService classLoaderService) {
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
                userCollctionType = classLoaderService.classForName(typeName);
            } catch (ClassLoadingException ex) {
                throw new MappingException(
                        '"' +
                        mappingProperty.getPersistentClass().getEntityName() +
                        '.' +
                        mappingProperty.getName() +
                        "\" must be mapped as collection whose attribute \"collection-type\" is \"" +
                        typeName +
                        "\", but the there is no java type names\"" +
                        typeName +
                        "\".",
                        ex);
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

	private static <V> Map<String, V> getStringMap(MetadataImplementor owner, String fieldName) {
		return XMetadataImpl.<String, V>getMap(owner, fieldName);
	}
	
	@SuppressWarnings("unchecked")
	private static <K, V> Map<K, V> getMap(MetadataImplementor owner, String fieldName) {
		return (Map<K, V>)getObject(owner, fieldName, Map.class);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getObject(MetadataImplementor owner, String fieldName, Class<T> fieldType) {
		Field field;
		try {
			field = MetadataImpl.class.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new AssertionError(
					"Internal bug: The class \""
					+ MetadataImpl.class
					+ "\" does not have field \""
					+ fieldName
					+ "\""
			);
		}
		if (!fieldType.isAssignableFrom(field.getType())) {
			throw new AssertionError(
					"Internal bug: The type of the field \""
					+ fieldName
					+ "\" of class \""
					+ MetadataImpl.class.getName()
					+ "\" is not \""
					+ fieldType.getName()
					+ "\" or its derived type"
			);
		}
		field.setAccessible(true);
		try {
			return (T)field.get(owner);
		} catch (IllegalAccessException ex) {
			throw new AssertionError("Internal bug");
		}
	}
	
	private interface Resource {
		
		String missInstrument(
                Class<?> entityClass,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentConstant,
                String instrumentExpectedPOMSection);
		
		String requiredHibernateObjectModelFactoryProvider(
                Class<?> entityClass,
                Class<HibernateObjectModelFactoryProvider> hibernateObjectModelFactoryProvider);
	}
}
