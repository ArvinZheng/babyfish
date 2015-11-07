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

import java.util.Map;

import org.babyfish.hibernate.XSessionFactory;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.EntityMode;
import org.hibernate.EntityNameResolver;
import org.hibernate.Interceptor;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.NullPrecedence;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.TempTableDdlTransactionHandling;
import org.hibernate.cache.spi.QueryCacheFactory;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.hql.spi.id.MultiTableBulkIdStrategy;
import org.hibernate.loader.BatchFetchStyle;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.EntityTuplizerFactory;

/**
 * @author Tao Chen
 */
public interface XSessionFactoryBuilder extends SessionFactoryBuilder {

    @Override
    XSessionFactoryBuilder applyValidatorFactory(Object validatorFactory);

	@Override
	XSessionFactoryBuilder applyBeanManager(Object beanManager);

	@Override
	XSessionFactoryBuilder applyName(String sessionFactoryName);

	@Override
	XSessionFactoryBuilder applyNameAsJndiName(boolean isJndiName);

	@Override
	XSessionFactoryBuilder applyAutoClosing(boolean enabled);

	@Override
	XSessionFactoryBuilder applyAutoFlushing(boolean enabled);

	@Override
	XSessionFactoryBuilder applyStatisticsSupport(boolean enabled);

	@Override
	XSessionFactoryBuilder applyInterceptor(Interceptor interceptor);

	@Override
	XSessionFactoryBuilder applyStatementInspector(StatementInspector statementInspector);

	@Override
	XSessionFactoryBuilder addSessionFactoryObservers(SessionFactoryObserver... observers);

	@Override
	XSessionFactoryBuilder applyCustomEntityDirtinessStrategy(CustomEntityDirtinessStrategy strategy);

	@Override
	XSessionFactoryBuilder addEntityNameResolver(EntityNameResolver... entityNameResolvers);

	@Override
	XSessionFactoryBuilder applyEntityNotFoundDelegate(EntityNotFoundDelegate entityNotFoundDelegate);

	@Override
	XSessionFactoryBuilder applyIdentifierRollbackSupport(boolean enabled);

	@Override
	XSessionFactoryBuilder applyDefaultEntityMode(EntityMode entityMode);

	@Override
	XSessionFactoryBuilder applyNullabilityChecking(boolean enabled);

	@Override
	XSessionFactoryBuilder applyLazyInitializationOutsideTransaction(boolean enabled);

	@Override
	XSessionFactoryBuilder applyEntityTuplizerFactory(EntityTuplizerFactory entityTuplizerFactory);

	@Override
	XSessionFactoryBuilder applyEntityTuplizer(EntityMode entityMode, Class<? extends EntityTuplizer> tuplizerClass);

	@Override
	XSessionFactoryBuilder applyMultiTableBulkIdStrategy(MultiTableBulkIdStrategy strategy);

	@Override
	XSessionFactoryBuilder applyTempTableDdlTransactionHandling(TempTableDdlTransactionHandling handling);

	@Override
	XSessionFactoryBuilder applyBatchFetchStyle(BatchFetchStyle style);

	@Override
	XSessionFactoryBuilder applyDefaultBatchFetchSize(int size);

	@Override
	XSessionFactoryBuilder applyMaximumFetchDepth(int depth);

	@Override
	XSessionFactoryBuilder applyDefaultNullPrecedence(NullPrecedence nullPrecedence);

	@Override
	XSessionFactoryBuilder applyOrderingOfInserts(boolean enabled);

	@Override
	XSessionFactoryBuilder applyOrderingOfUpdates(boolean enabled);

	@Override
	XSessionFactoryBuilder applyMultiTenancyStrategy(MultiTenancyStrategy strategy);

	@Override
	XSessionFactoryBuilder applyCurrentTenantIdentifierResolver(CurrentTenantIdentifierResolver resolver);

	@Override
	XSessionFactoryBuilder applyJtaTrackingByThread(boolean enabled);

	@Override
	XSessionFactoryBuilder applyPreferUserTransactions(boolean preferUserTransactions);

	@SuppressWarnings("rawtypes")
	@Override
	XSessionFactoryBuilder applyQuerySubstitutions(Map substitutions);

	@Override
	XSessionFactoryBuilder applyStrictJpaQueryLanguageCompliance(boolean enabled);

	@Override
	XSessionFactoryBuilder applyNamedQueryCheckingOnStartup(boolean enabled);

	@Override
	XSessionFactoryBuilder applySecondLevelCacheSupport(boolean enabled);

	@Override
	XSessionFactoryBuilder applyQueryCacheSupport(boolean enabled);

	@Override
	XSessionFactoryBuilder applyQueryCacheFactory(QueryCacheFactory factory);

	@Override
	XSessionFactoryBuilder applyCacheRegionPrefix(String prefix);

	@Override
	XSessionFactoryBuilder applyMinimalPutsForCaching(boolean enabled);

	@Override
	XSessionFactoryBuilder applyStructuredCacheEntries(boolean enabled);

	@Override
	XSessionFactoryBuilder applyDirectReferenceCaching(boolean enabled);

	@Override
	XSessionFactoryBuilder applyAutomaticEvictionOfCollectionCaches(boolean enabled);

	@Override
	XSessionFactoryBuilder applyJdbcBatchSize(int size);

	@Override
	XSessionFactoryBuilder applyJdbcBatchingForVersionedEntities(boolean enabled);

	@Override
	XSessionFactoryBuilder applyScrollableResultsSupport(boolean enabled);

	@Override
	XSessionFactoryBuilder applyResultSetsWrapping(boolean enabled);

	@Override
	XSessionFactoryBuilder applyGetGeneratedKeysSupport(boolean enabled);

	@Override
	XSessionFactoryBuilder applyJdbcFetchSize(int size);

	@Override
	XSessionFactoryBuilder applyConnectionReleaseMode(ConnectionReleaseMode connectionReleaseMode);

	@Override
	XSessionFactoryBuilder applySqlComments(boolean enabled);

	@Override
	XSessionFactoryBuilder applySqlFunction(String registrationName, SQLFunction sqlFunction);

	@Override
    XSessionFactory build();
}
