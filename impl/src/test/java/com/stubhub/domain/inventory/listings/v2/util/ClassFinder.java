package com.stubhub.domain.inventory.listings.v2.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * Test utility class to find all classes in a package.
 */
public class ClassFinder {
	public List<Class> findClassesInPackage(String basePackage) throws IOException,
			ClassNotFoundException {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
				resourcePatternResolver);

		List<Class> candidates = new ArrayList<Class>();
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
				+ resolveBasePackage(basePackage) + "/" + "**/*.class";
		Resource[] resources = resourcePatternResolver
				.getResources(packageSearchPath);
		for (Resource resource : resources) {
			if (resource.isReadable()) {
				MetadataReader metadataReader = metadataReaderFactory
						.getMetadataReader(resource);
				if (isCandidate(metadataReader)) {
					candidates.add(Class.forName(metadataReader
							.getClassMetadata().getClassName()));
				}
			}
		}
		for(Class clazz : candidates)
			System.out.println(clazz.getName());
		return candidates;
	}

	private String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils
				.resolvePlaceholders(basePackage));
	}

	private boolean isCandidate(MetadataReader metadataReader)
			throws ClassNotFoundException {
		try {
			Class c = Class.forName(metadataReader.getClassMetadata()
					.getClassName());
			if (c.isAnnotation() || c.isAnonymousClass() || c.isArray() || c.isEnum() || c.isInterface())
				return false;
			else
				return true;
//			if (c.getAnnotation(XmlRootElement.class) != null) {
//				return true;
//			}
		} catch (Throwable e) {
		}
		return false;
	}
}
