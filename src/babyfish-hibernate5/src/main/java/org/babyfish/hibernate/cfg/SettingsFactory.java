package org.babyfish.hibernate.cfg;

import java.util.Map;

import org.babyfish.util.LazyResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsFactory {

    private static final long serialVersionUID = -2662239924044486522L;
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsFactory.class);
    
    private static final String OBJECT_QUERY_TRANSLATOR_FACTORY = "babyfish.hibernate.object_query_translator_factory";

    public static final String ENABLE_LIMIT_IN_MEMORY = "babyfish.hibernate.enable_limit_in_memory";
    
    public static final String CREATE_ORACLE_DISTINCT_RANK = "babyfish.hibernate.create_oracle_distinct_rank";
    
    public static final String STRICT_DB_SCHEMA = "babyfish.hibernate.strict_db_schema";
    
    public static boolean isLimitInMemoryEnabled(Map<?, ?> properties) {
        return isTrue(properties, ENABLE_LIMIT_IN_MEMORY);
    }
    
    public static boolean isDistinctRankCreateable(Map<?, ?> properties) {
        return isTrue(properties, CREATE_ORACLE_DISTINCT_RANK);
    }
    
    public static boolean isDbSchemaStrict(Map<?, ?> properties) {
        return isTrue(properties, STRICT_DB_SCHEMA);
    }
    
    private static boolean isTrue(Map<?, ?> properties, String propertyName) {
        if (properties == null) {
            return false;
        }
        Object value = properties.get(propertyName);
        return "true".equals(value) || Boolean.TRUE.equals(value);
    }

//    @Override
//    protected final QueryTranslatorFactory createQueryTranslatorFactory(Properties properties, ServiceRegistry serviceRegistry) {
//        if (properties.containsKey(Environment.QUERY_TRANSLATOR)) {
//            throw new HibernateException(
//                    "the property \"" +
//                    Environment.QUERY_TRANSLATOR +
//                    "\" is deprecated by \"" +
//                    OBJECT_QUERY_TRANSLATOR_FACTORY +
//                    "\"");
//        }
//        String className = ConfigurationHelper.getString(
//                AvailableSettings.QUERY_TRANSLATOR, 
//                properties,
//                XQueryTranslatorFactoryImpl.class.getName());
//        LOGGER.info("Entity query translator: " + className);
//        final XQueryTranslatorFactory translatorFactory;
//        Class<?> xQueryTranslatorFactoryImplClass;
//        try {
//            xQueryTranslatorFactoryImplClass = ReflectHelper.classForName(className);
//        } catch (ClassNotFoundException ex) {
//            throw new HibernateException(
//                    LAZY_RESOURCE.get().notExistingXQueryTransalatorFactoryImpl(
//                            XQueryTranslatorFactory.class,
//                            className
//                    )
//            );
//        }
//        try {
//            translatorFactory = (XQueryTranslatorFactory)xQueryTranslatorFactoryImplClass.newInstance();
//        } catch (InstantiationException | IllegalAccessException ex) {
//            throw new HibernateException(
//                    LAZY_RESOURCE.get().failedToInstantiateXQueryTranslatorFactory(
//                            XQueryTranslatorFactory.class, 
//                            xQueryTranslatorFactoryImplClass
//                    ), 
//                    ex);
//        }
//        
//        return new QueryTranslatorFactory() {
//            
//            @SuppressWarnings({ "unchecked", "rawtypes" }) 
//            @Override
//            public QueryTranslator createQueryTranslator(
//                    String queryIdentifier,
//                    String queryString, 
//                    Map filters, 
//                    SessionFactoryImplementor factory,
//                    EntityGraphQueryHint entityGraphQueryHint) {
//                return translatorFactory.createQueryTranslator(
//                        queryIdentifier, 
//                        queryString, 
//                        XQueryPlan.currentPathPlanKey(), 
//                        filters, 
//                        factory,
//                        entityGraphQueryHint);
//            }
//            
//            @SuppressWarnings({ "unchecked", "rawtypes" }) 
//            @Override
//            public FilterTranslator createFilterTranslator(
//                    String queryIdentifier,
//                    String queryString, 
//                    Map filters, 
//                    SessionFactoryImplementor factory) {
//                return translatorFactory.createFilterTranslator(
//                        queryIdentifier, 
//                        queryString, 
//                        XQueryPlan.currentPathPlanKey(), 
//                        filters, 
//                        factory);
//            }
//        };
//    }

    private interface Resource {
        
        String notExistingXQueryTransalatorFactoryImpl(Class<?> interfaceType, String implementationClassName);
        
        String failedToInstantiateXQueryTranslatorFactory(Class<?> interfaceType, Class<?> implementationType);
    }
}
