package org.babyfish.hibernate.association;

import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;

public class JPAObjectModelPropertyAccessStrategy implements PropertyAccessStrategy {
	
	public static final JPAObjectModelPropertyAccessStrategy INSTANCE = 
			new JPAObjectModelPropertyAccessStrategy();
	
	@SuppressWarnings("rawtypes")
	@Override
	public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
		return new JPAObjectModelPropertyAccess(containerJavaType, propertyName);
	}
}
