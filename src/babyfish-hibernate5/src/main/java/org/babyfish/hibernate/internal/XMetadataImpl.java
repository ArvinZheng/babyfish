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
import java.util.UUID;

import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.boot.XMetadata;
import org.babyfish.hibernate.boot.XMetadataImplementor;
import org.babyfish.hibernate.boot.XSessionFactoryBuilder;
import org.babyfish.hibernate.boot.XSessionFactoryBuilderFactory;
import org.babyfish.lang.IllegalProgramException;
import org.hibernate.HibernateException;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.TypeDefinition;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
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
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.type.TypeResolver;

/**
 * @author Tao Chen
 */
public class XMetadataImpl extends MetadataImpl implements XMetadata, XMetadataImplementor {

    private static final long serialVersionUID = 2022753996073961324L;
    
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
	}
    
    @SuppressWarnings("rawtypes")
	public XMetadataImpl(MetadataImplementor metadata) {
    	this(
				metadata.getUUID(), 
				metadata.getMetadataBuildingOptions(), 
				metadata.getTypeResolver(), 
				getObject(metadata, "getIdentifierGeneratorFactory", MutableIdentifierGeneratorFactory.class), 
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
}
