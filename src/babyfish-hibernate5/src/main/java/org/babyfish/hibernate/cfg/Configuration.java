package org.babyfish.hibernate.cfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.boot.XMetadataSources;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BasicType;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import org.w3c.dom.Document;

public class Configuration extends org.hibernate.cfg.Configuration {

	public Configuration() {
		this(new XMetadataSources());
	}
	
	public Configuration(ServiceRegistry serviceRegistry) {
		this(new XMetadataSources(serviceRegistry));
	}
	
	public Configuration(XMetadataSources metadataSources) {
		super(metadataSources);
	}

	@Override
	public Configuration setProperties(Properties properties) {
		return (Configuration)super.setProperties(properties);
	}

	@Override
	public Configuration setProperty(String propertyName, String value) {
		return (Configuration)super.setProperty(propertyName, value);
	}

	@Override
	public Configuration addProperties(Properties properties) {
		return (Configuration)super.addProperties(properties);
	}

	@Override
	public Configuration configure() throws HibernateException {
		return (Configuration)super.configure();
	}

	@Override
	public Configuration configure(String resource) throws HibernateException {
		return (Configuration)super.configure(resource);
	}

	@Override
	public Configuration configure(URL url) throws HibernateException {
		return (Configuration)super.configure(url);
	}

	@Override
	public Configuration configure(File configFile) throws HibernateException {
		return (Configuration)super.configure(configFile);
	}

	@Override
	public Configuration configure(Document document) throws HibernateException {
		return (Configuration)super.configure(document);
	}

	@Override
	public Configuration registerTypeContributor(TypeContributor typeContributor) {
		return (Configuration)super.registerTypeContributor(typeContributor);
	}

	@Override
	public Configuration registerTypeOverride(BasicType type) {
		return (Configuration)super.registerTypeOverride(type);
	}

	@Override
	public Configuration registerTypeOverride(UserType type, String[] keys) {
		return (Configuration)super.registerTypeOverride(type, keys);
	}

	@Override
	public Configuration registerTypeOverride(CompositeUserType type, String[] keys) {
		return (Configuration)super.registerTypeOverride(type, keys);
	}

	@Override
	public Configuration addFile(String xmlFile) throws MappingException {
		return (Configuration)super.addFile(xmlFile);
	}

	@Override
	public Configuration addFile(File xmlFile) throws MappingException {
		return (Configuration)super.addFile(xmlFile);
	}

	@Override
	public Configuration addCacheableFile(File xmlFile) throws MappingException {
		return (Configuration)super.addCacheableFile(xmlFile);
	}

	@Override
	public Configuration addCacheableFileStrictly(File xmlFile)
			throws SerializationException, FileNotFoundException {
		return (Configuration)super.addCacheableFileStrictly(xmlFile);
	}

	@Override
	public Configuration addCacheableFile(String xmlFile) throws MappingException {
		return (Configuration)super.addCacheableFile(xmlFile);
	}

	@Deprecated
	@Override
	public Configuration addXML(String xml) throws MappingException {
		return (Configuration)super.addXML(xml);
	}

	@Override
	public Configuration addURL(URL url) throws MappingException {
		return (Configuration)super.addURL(url);
	}

	@Deprecated
	@Override
	public Configuration addDocument(Document doc) throws MappingException {
		return (Configuration)super.addDocument(doc);
	}

	@Override
	public Configuration addInputStream(InputStream xmlInputStream) throws MappingException {
		return (Configuration)super.addInputStream(xmlInputStream);
	}

	@Override
	public Configuration addResource(String resourceName, ClassLoader classLoader)
			throws MappingException {
		return (Configuration)super.addResource(resourceName, classLoader);
	}

	@Override
	public Configuration addResource(String resourceName) throws MappingException {
		return (Configuration)super.addResource(resourceName);
	}

	@Override
	public Configuration addClass(Class persistentClass) throws MappingException {
		return (Configuration)super.addClass(persistentClass);
	}

	@Override
	public Configuration addAnnotatedClass(Class annotatedClass) {
		return (Configuration)super.addAnnotatedClass(annotatedClass);
	}

	@Override
	public Configuration addPackage(String packageName) throws MappingException {
		return (Configuration)super.addPackage(packageName);
	}

	@Override
	public Configuration addJar(File jar) throws MappingException {
		return (Configuration)super.addJar(jar);
	}

	@Override
	public Configuration addDirectory(File dir) throws MappingException {
		return (Configuration)super.addDirectory(dir);
	}

	@Override
	public Configuration setInterceptor(Interceptor interceptor) {
		return (Configuration)super.setInterceptor(interceptor);
	}

	@Override
	public XSessionFactory buildSessionFactory(ServiceRegistry serviceRegistry) throws HibernateException {
		return (XSessionFactory)super.buildSessionFactory(serviceRegistry);
	}

	@Override
	public XSessionFactory buildSessionFactory() throws HibernateException {
		return (XSessionFactory)super.buildSessionFactory();
	}

	@Override
	public Configuration mergeProperties(Properties properties) {
		return (Configuration)super.mergeProperties(properties);
	}

}
