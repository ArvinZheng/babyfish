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
package org.babyfish.hibernate.jpa.boot.internal;

import java.lang.reflect.Field;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.babyfish.hibernate.boot.XMetadataImplementor;
import org.babyfish.hibernate.boot.XSessionFactoryBuilder;
import org.babyfish.hibernate.internal.XMetadataImpl;
import org.babyfish.hibernate.internal.XSessionFactoryImplementor;
import org.babyfish.hibernate.jpa.internal.XEntityManagerFactoryImpl;
import org.babyfish.persistence.XEntityManagerFactory;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.model.process.spi.MetadataBuildingProcess;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataBuilderImplementor;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.internal.SettingsImpl;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.internal.schemagen.JpaSchemaGenerator;

/**
 * @author Tao Chen
 */
public class XEntityManagerFactoryBuilderImpl extends EntityManagerFactoryBuilderImpl {
    
    private PersistenceUnitDescriptor persistenceUnit;
    
    private StandardServiceRegistry standardServiceRegistry;
    
    private SettingsImpl settings;
    
    private ManagedResources managedResources;
    
	private MetadataBuilderImplementor metamodelBuilder;
	
	private XMetadataImplementor metadata;
	
	public XEntityManagerFactoryBuilderImpl(
			PersistenceUnitInfo persistenceUnit, 
            Map<?, ?> integrationSettings,
            ClassLoader providedClassLoader) {
		this(new PersistenceUnitInfoDescriptor(persistenceUnit), integrationSettings, providedClassLoader);
	}
    
	public XEntityManagerFactoryBuilderImpl(
            PersistenceUnitDescriptor persistenceUnit, 
            Map<?, ?> integrationSettings,
            ClassLoader providedClassLoader) {
        super(persistenceUnit, integrationSettings, providedClassLoader);
        this.persistenceUnit = persistenceUnit;
        this.standardServiceRegistry = getObject(this, "standardServiceRegistry", StandardServiceRegistry.class);
        this.settings = getObject(this, "settings", SettingsImpl.class);
        this.managedResources = getObject(this, "managedResources", ManagedResources.class);
        this.metamodelBuilder = getObject(this, "metamodelBuilder", MetadataBuilderImplementor.class);
    }
    
    @Override
	public XMetadataImplementor getMetadata() {
		return (XMetadataImplementor)super.getMetadata();
	}
    
    private XMetadataImplementor metadata() {
    	if ( this.metadata == null ) {
			MetadataImplementor hibernateMetadata = 
					MetadataBuildingProcess.complete(
							this.managedResources, 
							this.metamodelBuilder.getMetadataBuildingOptions()
					);
			this.metadata = new XMetadataImpl(hibernateMetadata);
		}
		return metadata;
    }
    
    @Override
	public void generateSchema() {
    	try {
			SessionFactoryBuilder sfBuilder = this.metadata().getSessionFactoryBuilder();
			populate(sfBuilder, this.standardServiceRegistry);
			sfBuilder.build();

			JpaSchemaGenerator.performGeneration(
					this.metadata(), 
					this.getConfigurationValues(), 
					this.standardServiceRegistry
			);
		}
		catch (Exception e) {
			throw persistenceException( "Unable to build Hibernate SessionFactory", e );
		}

		// release this builder
		cancel();
	}

	@Override
	public XEntityManagerFactory build() {
		XSessionFactoryBuilder sfBuilder = this.metadata().getSessionFactoryBuilder();
		populate(sfBuilder, this.standardServiceRegistry);

		XSessionFactoryImplementor sessionFactory;
		try {
			sessionFactory = (XSessionFactoryImplementor)sfBuilder.build();
		}
		catch (Exception e) {
			throw persistenceException("Unable to build Hibernate SessionFactory", e );
		}

		JpaSchemaGenerator.performGeneration(
				metadata(), 
				this.getConfigurationValues(), 
				this.standardServiceRegistry
		);

		return new XEntityManagerFactoryImpl(
				this.persistenceUnit.getName(),
				sessionFactory,
				this.metadata(),
				this.settings,
				this.getConfigurationValues()
		);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getObject(EntityManagerFactoryBuilderImpl owner, String fieldName, Class<T> fieldType) {
		Field field;
		try {
			field = EntityManagerFactoryBuilderImpl.class.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new AssertionError(
					"Internal bug: The class \""
					+ EntityManagerFactoryBuilderImpl.class
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
					+ EntityManagerFactoryBuilderImpl.class.getName()
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
	
	private PersistenceException persistenceException(String message, Exception cause) {
		return new PersistenceException(
				this.getExceptionHeader() + message,
				cause
		);
	}

	private String getExceptionHeader() {
		return "[PersistenceUnit: " + this.persistenceUnit.getName() + "] ";
	}
}
