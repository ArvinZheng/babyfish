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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.boot.XMetadataImplementor;
import org.babyfish.hibernate.boot.XSessionFactoryBuilder;
import org.babyfish.hibernate.dialect.InstallableDialect;
import org.babyfish.hibernate.event.ObjectModelMergeEventListener;
import org.babyfish.hibernate.hql.XQueryPlanCache;
import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.metadata.Metadatas;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.EntityMode;
import org.hibernate.EntityNameResolver;
import org.hibernate.Interceptor;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.NullPrecedence;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.TempTableDdlTransactionHandling;
import org.hibernate.boot.internal.SessionFactoryBuilderImpl;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.cache.spi.QueryCacheFactory;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.hql.spi.id.MultiTableBulkIdStrategy;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.loader.BatchFetchStyle;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.hibernate.service.internal.AbstractServiceRegistryImpl;
import org.hibernate.service.spi.ServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.EntityTuplizerFactory;

/**
 * @author Tao Chen
 */
public class XSessionFactoryBuilderImpl extends SessionFactoryBuilderImpl implements XSessionFactoryBuilder {
    
    private static final Field QUERY_PLAN_CACHE_FIELD;
    
    private static final Method ABSTACT_SERVICE_RESGISTRY_IMPL_CREATE_SERVICE_BINDING;
    
    private static boolean pathPlanKeyVlidationSuspended;
    
    public XSessionFactoryBuilderImpl(XMetadataImplementor metadata) {
		super(metadata);
	}

    @Override
	public XSessionFactoryBuilder applyValidatorFactory(Object validatorFactory) {
		return (XSessionFactoryBuilder)super.applyValidatorFactory(validatorFactory);
	}

	@Override
	public XSessionFactoryBuilder applyBeanManager(Object beanManager) {
		return (XSessionFactoryBuilder)super.applyBeanManager(beanManager);
	}

	@Override
	public XSessionFactoryBuilder applyName(String sessionFactoryName) {
		return (XSessionFactoryBuilder)super.applyName(sessionFactoryName);
	}

	@Override
	public XSessionFactoryBuilder applyNameAsJndiName(boolean isJndiName) {
		return (XSessionFactoryBuilder)super.applyNameAsJndiName(isJndiName);
	}

	@Override
	public XSessionFactoryBuilder applyAutoClosing(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyAutoClosing(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyAutoFlushing(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyAutoFlushing(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyJtaTrackingByThread(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyJtaTrackingByThread(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyPreferUserTransactions(boolean preferUserTransactions) {
		return (XSessionFactoryBuilder)super.applyPreferUserTransactions(preferUserTransactions);
	}

	@Override
	public XSessionFactoryBuilder applyStatisticsSupport(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyStatisticsSupport(enabled);
	}

	@Override
	public XSessionFactoryBuilder addSessionFactoryObservers(SessionFactoryObserver... observers) {
		return (XSessionFactoryBuilder)super.addSessionFactoryObservers(observers);
	}

	@Override
	public XSessionFactoryBuilder applyInterceptor(Interceptor interceptor) {
		return (XSessionFactoryBuilder)super.applyInterceptor(interceptor);
	}

	@Override
	public XSessionFactoryBuilder applyStatementInspector(StatementInspector statementInspector) {
		return (XSessionFactoryBuilder)super.applyStatementInspector(statementInspector);
	}

	@Override
	public XSessionFactoryBuilder applyCustomEntityDirtinessStrategy(CustomEntityDirtinessStrategy strategy) {
		return (XSessionFactoryBuilder)super.applyCustomEntityDirtinessStrategy(strategy);
	}

	@Override
	public XSessionFactoryBuilder addEntityNameResolver(EntityNameResolver... entityNameResolvers) {
		return (XSessionFactoryBuilder)super.addEntityNameResolver(entityNameResolvers);
	}

	@Override
	public XSessionFactoryBuilder applyEntityNotFoundDelegate(EntityNotFoundDelegate entityNotFoundDelegate) {
		return (XSessionFactoryBuilder)super.applyEntityNotFoundDelegate(entityNotFoundDelegate);
	}

	@Override
	public XSessionFactoryBuilder applyIdentifierRollbackSupport(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyIdentifierRollbackSupport(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyDefaultEntityMode(EntityMode entityMode) {
		return (XSessionFactoryBuilder)super.applyDefaultEntityMode(entityMode);
	}

	@Override
	public XSessionFactoryBuilder applyNullabilityChecking(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyNullabilityChecking(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyLazyInitializationOutsideTransaction(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyLazyInitializationOutsideTransaction(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyEntityTuplizerFactory(EntityTuplizerFactory entityTuplizerFactory) {
		return (XSessionFactoryBuilder)super.applyEntityTuplizerFactory(entityTuplizerFactory);
	}

	@Override
	public XSessionFactoryBuilder applyEntityTuplizer(EntityMode entityMode,
			Class<? extends EntityTuplizer> tuplizerClass) {
		return (XSessionFactoryBuilder)super.applyEntityTuplizer(entityMode, tuplizerClass);
	}

	@Override
	public XSessionFactoryBuilder applyMultiTableBulkIdStrategy(MultiTableBulkIdStrategy strategy) {
		return (XSessionFactoryBuilder)super.applyMultiTableBulkIdStrategy(strategy);
	}

	@Override
	public XSessionFactoryBuilder applyTempTableDdlTransactionHandling(TempTableDdlTransactionHandling handling) {
		return (XSessionFactoryBuilder)super.applyTempTableDdlTransactionHandling(handling);
	}

	@Override
	public XSessionFactoryBuilder applyBatchFetchStyle(BatchFetchStyle style) {
		return (XSessionFactoryBuilder)super.applyBatchFetchStyle(style);
	}

	@Override
	public XSessionFactoryBuilder applyDefaultBatchFetchSize(int size) {
		return (XSessionFactoryBuilder)super.applyDefaultBatchFetchSize(size);
	}

	@Override
	public XSessionFactoryBuilder applyMaximumFetchDepth(int depth) {
		return (XSessionFactoryBuilder)super.applyMaximumFetchDepth(depth);
	}

	@Override
	public XSessionFactoryBuilder applyDefaultNullPrecedence(NullPrecedence nullPrecedence) {
		return (XSessionFactoryBuilder)super.applyDefaultNullPrecedence(nullPrecedence);
	}

	@Override
	public XSessionFactoryBuilder applyOrderingOfInserts(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyOrderingOfInserts(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyOrderingOfUpdates(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyOrderingOfUpdates(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyMultiTenancyStrategy(MultiTenancyStrategy strategy) {
		return (XSessionFactoryBuilder)super.applyMultiTenancyStrategy(strategy);
	}

	@Override
	public XSessionFactoryBuilder applyCurrentTenantIdentifierResolver(CurrentTenantIdentifierResolver resolver) {
		return (XSessionFactoryBuilder)super.applyCurrentTenantIdentifierResolver(resolver);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public XSessionFactoryBuilder applyQuerySubstitutions(Map substitutions) {
		return (XSessionFactoryBuilder)super.applyQuerySubstitutions(substitutions);
	}

	@Override
	public XSessionFactoryBuilder applyStrictJpaQueryLanguageCompliance(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyStrictJpaQueryLanguageCompliance(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyNamedQueryCheckingOnStartup(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyNamedQueryCheckingOnStartup(enabled);
	}

	@Override
	public XSessionFactoryBuilder applySecondLevelCacheSupport(boolean enabled) {
		return (XSessionFactoryBuilder)super.applySecondLevelCacheSupport(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyQueryCacheSupport(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyQueryCacheSupport(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyQueryCacheFactory(QueryCacheFactory factory) {
		return (XSessionFactoryBuilder)super.applyQueryCacheFactory(factory);
	}

	@Override
	public XSessionFactoryBuilder applyCacheRegionPrefix(String prefix) {
		return (XSessionFactoryBuilder)super.applyCacheRegionPrefix(prefix);
	}

	@Override
	public XSessionFactoryBuilder applyMinimalPutsForCaching(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyMinimalPutsForCaching(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyStructuredCacheEntries(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyStructuredCacheEntries(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyDirectReferenceCaching(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyDirectReferenceCaching(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyAutomaticEvictionOfCollectionCaches(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyAutomaticEvictionOfCollectionCaches(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyJdbcBatchSize(int size) {
		return (XSessionFactoryBuilder)super.applyJdbcBatchSize(size);
	}

	@Override
	public XSessionFactoryBuilder applyJdbcBatchingForVersionedEntities(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyJdbcBatchingForVersionedEntities(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyScrollableResultsSupport(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyScrollableResultsSupport(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyResultSetsWrapping(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyResultSetsWrapping(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyGetGeneratedKeysSupport(boolean enabled) {
		return (XSessionFactoryBuilder)super.applyGetGeneratedKeysSupport(enabled);
	}

	@Override
	public XSessionFactoryBuilder applyJdbcFetchSize(int size) {
		return (XSessionFactoryBuilder)super.applyJdbcFetchSize(size);
	}

	@Override
	public XSessionFactoryBuilder applyConnectionReleaseMode(ConnectionReleaseMode connectionReleaseMode) {
		return (XSessionFactoryBuilder)super.applyConnectionReleaseMode(connectionReleaseMode);
	}

	@Override
	public XSessionFactoryBuilder applySqlComments(boolean enabled) {
		return (XSessionFactoryBuilder)super.applySqlComments(enabled);
	}

	@Override
	public XSessionFactoryBuilder applySqlFunction(String registrationName, SQLFunction sqlFunction) {
		return (XSessionFactoryBuilder)super.applySqlFunction(registrationName, sqlFunction);
	}

	@Override
    public XSessionFactory build() {
		
        replacePersisterClassResolver((AbstractServiceRegistryImpl)this.getServiceRegistry());
        
        SessionFactoryImpl factory;
        pathPlanKeyVlidationSuspended = true;
        try {
            factory = (SessionFactoryImpl)super.build();
        } finally {
            pathPlanKeyVlidationSuspended = false;
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
    
    protected XQueryPlanCache createQueryPlanCache(SessionFactoryImplementor factory) {
        return new XQueryPlanCache(factory);
    }
    
    static void setQueryPlanceCache(SessionFactoryImpl factory, QueryPlanCache queryPlanCache) {
        try {
            QUERY_PLAN_CACHE_FIELD.set(factory, queryPlanCache);
        } catch (IllegalAccessException ex) {
            throw new AssertionError();
        }
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
    
    static {
        Field queryPlanCacheField;
        try {
            queryPlanCacheField = SessionFactoryImpl.class.getDeclaredField("queryPlanCache");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError();
        }
        queryPlanCacheField.setAccessible(true);
        
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
        
        QUERY_PLAN_CACHE_FIELD = queryPlanCacheField;
        ABSTACT_SERVICE_RESGISTRY_IMPL_CREATE_SERVICE_BINDING = abstractServiceRegistryImplCreateServiceBinding;
    }
}
