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

import javax.persistence.AttributeConverter;
import javax.persistence.SharedCacheMode;

import org.babyfish.hibernate.boot.XMetadataBuilder;
import org.babyfish.hibernate.boot.XMetadataBuilderImplementor;
import org.babyfish.hibernate.boot.XMetadataImplementor;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.CacheRegionDefinition;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.scan.spi.Scanner;
import org.hibernate.boot.archive.spi.ArchiveDescriptorFactory;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.model.IdGeneratorStrategyInterpreter;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.AttributeConverterDefinition;
import org.hibernate.cfg.MetadataSourceType;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.type.BasicType;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
public class XMetadataBuilderImpl 
extends MetadataBuilderImpl 
implements XMetadataBuilder, XMetadataBuilderImplementor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(XMetadataBuilderImpl.class);

	public XMetadataBuilderImpl(MetadataSources sources, StandardServiceRegistry serviceRegistry) {
		super(sources, serviceRegistry);
	}

	public XMetadataBuilderImpl(MetadataSources sources) {
		super(sources);
	}

	@Override
	public XMetadataBuilder applyImplicitSchemaName(String implicitSchemaName) {
		return (XMetadataBuilder)super.applyImplicitSchemaName(implicitSchemaName);
	}

	@Override
	public XMetadataBuilder applyImplicitCatalogName(String implicitCatalogName) {
		return (XMetadataBuilder)super.applyImplicitCatalogName(implicitCatalogName);
	}

	@Override
	public XMetadataBuilder applyImplicitNamingStrategy(ImplicitNamingStrategy namingStrategy) {
		return (XMetadataBuilder)super.applyImplicitNamingStrategy(namingStrategy);
	}

	@Override
	public XMetadataBuilder applyPhysicalNamingStrategy(PhysicalNamingStrategy namingStrategy) {
		return (XMetadataBuilder)super.applyPhysicalNamingStrategy(namingStrategy);
	}

	@Override
	public XMetadataBuilder applyReflectionManager(ReflectionManager reflectionManager) {
		return (XMetadataBuilder)super.applyReflectionManager(reflectionManager);
	}

	@Override
	public XMetadataBuilder applySharedCacheMode(SharedCacheMode sharedCacheMode) {
		return (XMetadataBuilder)super.applySharedCacheMode(sharedCacheMode);
	}

	@Override
	public XMetadataBuilder applyAccessType(AccessType implicitCacheAccessType) {
		return (XMetadataBuilder)super.applyAccessType(implicitCacheAccessType);
	}

	@Override
	public XMetadataBuilder applyIndexView(IndexView jandexView) {
		return (XMetadataBuilder)super.applyIndexView(jandexView);
	}

	@Override
	public XMetadataBuilder applyScanOptions(ScanOptions scanOptions) {
		return (XMetadataBuilder)super.applyScanOptions(scanOptions);
	}

	@Override
	public XMetadataBuilder applyScanEnvironment(ScanEnvironment scanEnvironment) {
		return (XMetadataBuilder)super.applyScanEnvironment(scanEnvironment);
	}

	@Override
	public XMetadataBuilder applyScanner(Scanner scanner) {
		return (XMetadataBuilder)super.applyScanner(scanner);
	}

	@Override
	public XMetadataBuilder applyArchiveDescriptorFactory(ArchiveDescriptorFactory factory) {
		return (XMetadataBuilder)super.applyArchiveDescriptorFactory(factory);
	}

	@Override
	public XMetadataBuilder enableExplicitDiscriminatorsForJoinedSubclassSupport(boolean supported) {
		return (XMetadataBuilder)super.enableExplicitDiscriminatorsForJoinedSubclassSupport(supported);
	}

	@Override
	public XMetadataBuilder enableImplicitDiscriminatorsForJoinedSubclassSupport(boolean supported) {
		return (XMetadataBuilder)super.enableImplicitDiscriminatorsForJoinedSubclassSupport(supported);
	}

	@Override
	public XMetadataBuilder enableImplicitForcingOfDiscriminatorsInSelect(boolean supported) {
		return (XMetadataBuilder)super.enableImplicitForcingOfDiscriminatorsInSelect(supported);
	}

	@Override
	public XMetadataBuilder enableGlobalNationalizedCharacterDataSupport(boolean enabled) {
		return (XMetadataBuilder)super.enableGlobalNationalizedCharacterDataSupport(enabled);
	}

	@Override
	public XMetadataBuilder applyBasicType(BasicType type) {
		return (XMetadataBuilder)super.applyBasicType(type);
	}

	@Override
	public XMetadataBuilder applyBasicType(BasicType type, String... keys) {
		return (XMetadataBuilder)super.applyBasicType(type, keys);
	}

	@Override
	public XMetadataBuilder applyBasicType(UserType type, String... keys) {
		return (XMetadataBuilder)super.applyBasicType(type, keys);
	}

	@Override
	public XMetadataBuilder applyBasicType(CompositeUserType type, String... keys) {
		return (XMetadataBuilder)super.applyBasicType(type, keys);
	}

	@Override
	public XMetadataBuilder applyTypes(TypeContributor typeContributor) {
		return (XMetadataBuilder)super.applyTypes(typeContributor);
	}
	
	@Override
	public XMetadataBuilder applyCacheRegionDefinition(CacheRegionDefinition cacheRegionDefinition) {
		return (XMetadataBuilder)super.applyCacheRegionDefinition(cacheRegionDefinition);
	}

	@Override
	public XMetadataBuilder applyTempClassLoader(ClassLoader tempClassLoader) {
		return (XMetadataBuilder)super.applyTempClassLoader(tempClassLoader);
	}

	@Override
	public XMetadataBuilder applySourceProcessOrdering(MetadataSourceType... sourceTypes) {
		return (XMetadataBuilder)super.applySourceProcessOrdering(sourceTypes);
	}

	@Override
	public XMetadataBuilder allowSpecjSyntax() {
		return (XMetadataBuilder)super.allowSpecjSyntax();
	}

	@Override
	public XMetadataBuilder applySqlFunction(String functionName, SQLFunction function) {
		return (XMetadataBuilder)super.applySqlFunction(functionName, function);
	}

	@Override
	public XMetadataBuilder applyAuxiliaryDatabaseObject(AuxiliaryDatabaseObject auxiliaryDatabaseObject) {
		return (XMetadataBuilder)super.applyAuxiliaryDatabaseObject(auxiliaryDatabaseObject);
	}

	@Override
	public XMetadataBuilder applyAttributeConverter(AttributeConverterDefinition definition) {
		return (XMetadataBuilder)super.applyAttributeConverter(definition);
	}

	@Override
	public XMetadataBuilder applyAttributeConverter(Class<? extends AttributeConverter> attributeConverterClass) {
		return (XMetadataBuilder)super.applyAttributeConverter(attributeConverterClass);
	}

	@Override
	public XMetadataBuilder applyAttributeConverter(Class<? extends AttributeConverter> attributeConverterClass,
			boolean autoApply) {
		return (XMetadataBuilder)super.applyAttributeConverter(attributeConverterClass, autoApply);
	}

	@Override
	public XMetadataBuilder applyAttributeConverter(AttributeConverter attributeConverter) {
		return (XMetadataBuilder)super.applyAttributeConverter(attributeConverter);
	}

	@Override
	public XMetadataBuilder applyAttributeConverter(AttributeConverter attributeConverter, boolean autoApply) {
		return (XMetadataBuilder)super.applyAttributeConverter(attributeConverter, autoApply);
	}

	@Override
	public XMetadataBuilder enableNewIdentifierGeneratorSupport(boolean enabled) {
		return (XMetadataBuilder)super.enableNewIdentifierGeneratorSupport(enabled);
	}

	@Override
	public XMetadataBuilder applyIdGenerationTypeInterpreter(IdGeneratorStrategyInterpreter interpreter) {
		return (XMetadataBuilder)super.applyIdGenerationTypeInterpreter(interpreter);
	}

	@Override
	public XMetadataImplementor build() {
		MetadataImplementor metadata = super.build();
		return new XMetadataImpl(
				metadata.getUUID(), 
				metadata.getMetadataBuildingOptions(), 
				metadata.getTypeResolver(), 
				metadata.getIdGeneratorDefinations(),
				metadata.getEntityBindings(),
				metadata.getMappedSuperclassMappingsCopy(),
				metadata.getCollectionBindings(), 
				metadata.getTypeDefinitions(), 
				metadata.getFilterDefinitions(), 
				metadata.getFetchProfiles(), 
				metadata.getImports(), 
				metadata.getIdGeneratorDefinitions(), 
				metadata.getNamedQueryDefinitions(),
				metadata.getNamedNativeQueryDefinitions(),
				metadata.getNamedProcedureCallDefinitions(), 
				metadata.getResultSetMappingDefinitions(),
				metadata.getNamedEntityGraphs(), 
				metadata.getSqlFunctionMap(), 
				metadata.getDatabase()
		);
	}
}
