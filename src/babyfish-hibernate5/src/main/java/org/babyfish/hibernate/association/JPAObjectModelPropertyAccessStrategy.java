package org.babyfish.hibernate.association;

import org.babyfish.lang.Singleton;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;

public class JPAObjectModelPropertyAccessStrategy extends Singleton implements PropertyAccessStrategy {

	protected JPAObjectModelPropertyAccessStrategy() {}
	
	public static JPAObjectModelPropertyAccessStrategy getInstance() {
		return getInstance(JPAObjectModelPropertyAccessStrategy.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
		return new JPAObjectModelPropertyAccess(containerJavaType, propertyName);
	}
}
