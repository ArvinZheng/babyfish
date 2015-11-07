package org.babyfish.hibernate.boot;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.MetadataBuilderFactory;
import org.hibernate.boot.spi.MetadataBuilderImplementor;

public interface XMetadataBuilderFactory extends MetadataBuilderFactory {

	@Override
	XMetadataBuilderImplementor getMetadataBuilder(
			MetadataSources metadatasources, 
			MetadataBuilderImplementor defaultBuilder
	);
}
