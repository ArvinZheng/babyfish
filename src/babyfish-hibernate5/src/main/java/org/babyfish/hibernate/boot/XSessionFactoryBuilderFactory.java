package org.babyfish.hibernate.boot;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;

public interface XSessionFactoryBuilderFactory extends SessionFactoryBuilderFactory{
	
	@Override
	XSessionFactoryBuilder getSessionFactoryBuilder(MetadataImplementor metadata,
			SessionFactoryBuilderImplementor defaultBuilder);
}
