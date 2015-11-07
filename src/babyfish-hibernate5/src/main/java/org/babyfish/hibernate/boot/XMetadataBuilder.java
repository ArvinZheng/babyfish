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
package org.babyfish.hibernate.boot;

import javax.persistence.AttributeConverter;
import javax.persistence.SharedCacheMode;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.CacheRegionDefinition;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.scan.spi.Scanner;
import org.hibernate.boot.archive.spi.ArchiveDescriptorFactory;
import org.hibernate.boot.model.IdGeneratorStrategyInterpreter;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.AttributeConverterDefinition;
import org.hibernate.cfg.MetadataSourceType;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.type.BasicType;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import org.jboss.jandex.IndexView;

/**
 * @author Tao Chen
 */
public interface XMetadataBuilder extends MetadataBuilder {

    @Override
	XMetadataBuilder applyImplicitCatalogName(String implicitCatalogName);

	@Override
	XMetadataBuilder applyImplicitSchemaName(String implicitSchemaName);

	@Override
	XMetadataBuilder applyImplicitNamingStrategy(ImplicitNamingStrategy namingStrategy);

	@Override
	XMetadataBuilder applyPhysicalNamingStrategy(PhysicalNamingStrategy namingStrategy);

	@Override
	XMetadataBuilder applyReflectionManager(ReflectionManager reflectionManager);

	@Override
	XMetadataBuilder applySharedCacheMode(SharedCacheMode cacheMode);

	@Override
	XMetadataBuilder applyAccessType(AccessType accessType);

	@Override
	XMetadataBuilder applyIndexView(IndexView jandexView);

	@Override
	XMetadataBuilder applyScanOptions(ScanOptions scanOptions);

	@Override
	XMetadataBuilder applyScanEnvironment(ScanEnvironment scanEnvironment);

	@Override
	XMetadataBuilder applyScanner(Scanner scanner);

	@Override
	XMetadataBuilder applyArchiveDescriptorFactory(ArchiveDescriptorFactory factory);

	@Override
	XMetadataBuilder enableNewIdentifierGeneratorSupport(boolean enable);

	@Override
	XMetadataBuilder enableExplicitDiscriminatorsForJoinedSubclassSupport(boolean enabled);

	@Override
	XMetadataBuilder enableImplicitDiscriminatorsForJoinedSubclassSupport(boolean enabled);

	@Override
	XMetadataBuilder enableImplicitForcingOfDiscriminatorsInSelect(boolean supported);

	@Override
	XMetadataBuilder enableGlobalNationalizedCharacterDataSupport(boolean enabled);

	@Override
	XMetadataBuilder applyBasicType(BasicType type);

	@Override
	XMetadataBuilder applyBasicType(BasicType type, String... keys);

	@Override
	XMetadataBuilder applyBasicType(UserType type, String... keys);

	@Override
	XMetadataBuilder applyBasicType(CompositeUserType type, String... keys);

	@Override
	XMetadataBuilder applyTypes(TypeContributor typeContributor);

	@Override
	XMetadataBuilder applyCacheRegionDefinition(CacheRegionDefinition cacheRegionDefinition);

	@Override
	XMetadataBuilder applyTempClassLoader(ClassLoader tempClassLoader);

	@Override
	XMetadataBuilder applySourceProcessOrdering(MetadataSourceType... sourceTypes);

	@Override
	XMetadataBuilder applySqlFunction(String functionName, SQLFunction function);

	@Override
	XMetadataBuilder applyAuxiliaryDatabaseObject(AuxiliaryDatabaseObject auxiliaryDatabaseObject);

	@Override
	XMetadataBuilder applyAttributeConverter(AttributeConverterDefinition definition);

	@SuppressWarnings("rawtypes")
	@Override
	XMetadataBuilder applyAttributeConverter(Class<? extends AttributeConverter> attributeConverterClass);

	@SuppressWarnings("rawtypes")
	@Override
	XMetadataBuilder applyAttributeConverter(Class<? extends AttributeConverter> attributeConverterClass,
			boolean autoApply);

	@SuppressWarnings("rawtypes")
	@Override
	XMetadataBuilder applyAttributeConverter(AttributeConverter attributeConverter);

	@SuppressWarnings("rawtypes")
	@Override
	XMetadataBuilder applyAttributeConverter(AttributeConverter attributeConverter, boolean autoApply);

	@Override
	XMetadataBuilder applyIdGenerationTypeInterpreter(IdGeneratorStrategyInterpreter interpreter);

	@Override
    public XMetadata build();
}
