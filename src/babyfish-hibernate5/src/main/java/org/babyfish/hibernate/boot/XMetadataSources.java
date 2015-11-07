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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.babyfish.hibernate.internal.XMetadataBuilderImpl;
import org.babyfish.lang.IllegalProgramException;
import org.hibernate.HibernateException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.archive.spi.InputStreamAccess;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataBuilderFactory;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SerializationException;
import org.w3c.dom.Document;

/**
 * @author Tao Chen
 */
public class XMetadataSources extends MetadataSources {
  
	private static final long serialVersionUID = 5740062934084589251L;

    public XMetadataSources() {
    }
    
    public XMetadataSources(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public XMetadataSources addAnnotatedClass(Class annotatedClass) {
        return (XMetadataSources)super.addAnnotatedClass(annotatedClass);
    }

    @Override
    public XMetadataSources addPackage(String packageName) {
        return (XMetadataSources)super.addPackage(packageName);
    }

    @Override
    public XMetadataSources addResource(String name) {
        return (XMetadataSources)super.addResource(name);
    }

    @SuppressWarnings({ "rawtypes", "deprecation" })
    @Override
    public XMetadataSources addClass(Class entityClass) {
        return (XMetadataSources)super.addClass(entityClass);
    }

    @Override
    public XMetadataSources addFile(String path) {
        return (XMetadataSources)super.addFile(path);
    }

    @Override
    public XMetadataSources addFile(File file) {
        return (XMetadataSources)super.addFile(file);
    }

    @Override
    public XMetadataSources addCacheableFile(String path) {
        return (XMetadataSources)super.addCacheableFile(path);
    }

    @Override
    public XMetadataSources addCacheableFile(File file) {
        return (XMetadataSources)super.addCacheableFile(file);
    }

    @Override
    public XMetadataSources addInputStream(InputStream xmlInputStream) {
        return (XMetadataSources)super.addInputStream(xmlInputStream);
    }

    @Override
    public XMetadataSources addURL(URL url) {
        return (XMetadataSources)super.addURL(url);
    }

    @SuppressWarnings("deprecation")
	@Override
    public XMetadataSources addDocument(Document document) {
        return (XMetadataSources)super.addDocument(document);
    }

    @Override
    public XMetadataSources addJar(File jar) {
        return (XMetadataSources)super.addJar(jar);
    }

    @Override
    public XMetadataSources addDirectory(File dir) {
        return (XMetadataSources)super.addDirectory(dir);
    }

	@Override
	public XMetadataSources addAnnotatedClassName(String annotatedClassName) {
		return (XMetadataSources)super.addAnnotatedClassName(annotatedClassName);
	}

	@Override
	public XMetadataSources addPackage(Package packageRef) {
		return (XMetadataSources)super.addPackage(packageRef);
	}

	@Override
	public XMetadataSources addCacheableFileStrictly(File file) throws SerializationException, FileNotFoundException {
		return (XMetadataSources)super.addCacheableFileStrictly(file);
	}

	@Override
	public XMetadataSources addInputStream(InputStreamAccess xmlInputStreamAccess) {
		return (XMetadataSources)super.addInputStream(xmlInputStreamAccess);
	}

    @Override
    public XMetadata buildMetadata() {
        return this.getMetadataBuilder().build();
    }
    
    @Override
    public XMetadataBuilder getMetadataBuilder() {
    	XMetadataBuilderImpl defaultBuilder = new XMetadataBuilderImpl( this );
		return getCustomBuilderOrDefault( defaultBuilder );
    }
    
    @Override
	public XMetadataBuilder getMetadataBuilder(StandardServiceRegistry serviceRegistry) {
    	XMetadataBuilderImpl defaultBuilder = new XMetadataBuilderImpl(this, serviceRegistry);
		return getCustomBuilderOrDefault(defaultBuilder);
	}

	@Override
	public XMetadata buildMetadata(StandardServiceRegistry serviceRegistry) {
		return this.getMetadataBuilder(serviceRegistry).build();
	}
	
    protected XMetadataBuilder createMetdataBuilder() {
        return new XMetadataBuilderImpl(this);
    }
    
    private XMetadataBuilder getCustomBuilderOrDefault(XMetadataBuilderImpl defaultBuilder) {
		final ClassLoaderService cls = this.getServiceRegistry().getService( ClassLoaderService.class );
		final Collection<MetadataBuilderFactory> discoveredBuilderFactories = 
				cls.loadJavaServices(MetadataBuilderFactory.class);

		XMetadataBuilder builder = null;
		List<String> activeFactoryNames = null;

		for (MetadataBuilderFactory discoveredBuilderFactory : discoveredBuilderFactories) {
			if (!(discoveredBuilderFactory instanceof XMetadataBuilderFactory)) {
				throw new IllegalProgramException();
			}
			final XMetadataBuilder returnedBuilder = 
					((XMetadataBuilderFactory)discoveredBuilderFactory)
					.getMetadataBuilder(this, defaultBuilder);
			if ( returnedBuilder != null ) {
				if ( activeFactoryNames == null ) {
					activeFactoryNames = new ArrayList<String>();
				}
				activeFactoryNames.add( discoveredBuilderFactory.getClass().getName() );
				builder = returnedBuilder;
			}
		}

		if ( activeFactoryNames != null && activeFactoryNames.size() > 1 ) {
			throw new HibernateException(
					"Multiple active MetadataBuilder definitions were discovered : " +
							StringHelper.join( ", ", activeFactoryNames )
			);
		}

		return builder != null ? builder : defaultBuilder;
	}
}
