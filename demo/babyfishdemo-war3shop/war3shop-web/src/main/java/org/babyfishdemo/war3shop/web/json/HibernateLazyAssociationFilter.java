package org.babyfishdemo.war3shop.web.json;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import com.alibaba.fastjson.serializer.PropertyFilter;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public class HibernateLazyAssociationFilter implements PropertyFilter {
    
    public static final HibernateLazyAssociationFilter INSTANCE =
            new HibernateLazyAssociationFilter();
    
    private HibernateLazyAssociationFilter() {
        
    }

    @Override
    public boolean apply(Object object, String name, Object value) {
        if (value instanceof HibernateProxy) {
            HibernateProxy hibernateProxy = (HibernateProxy)value;
            if (hibernateProxy.getHibernateLazyInitializer().isUninitialized()) {
                return false;
            }
        } else if (value instanceof PersistentCollection) {
            PersistentCollection persistentCollection = (PersistentCollection)value;
            if (!persistentCollection.wasInitialized()) {
                return false;
            }
        }
        return true;
    }
}
