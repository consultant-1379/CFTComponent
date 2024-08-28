/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.nms.mediation.camel.components.cftcomponent.cftpluginreader;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CustomCftException;

/**
 * @author ekakama
 * 
 */
public class PluginJarReaderTest {
	
	String userDir = System.getProperty("user.dir");
	
	private static final String JAR_FILE_PATH = "/src/test/resources/";
	
	PluginJarReader pluginReader;

	@Before
	public void setUp() throws CustomCftException {
		pluginReader = PluginJarReader.getPluginReaderInstance(userDir + JAR_FILE_PATH);
	}

	@Test
	public void loadIsLoadingJarFileCorrectly() throws CustomCftException {
		final boolean isJarLoaded = pluginReader.load();
		assertTrue(isJarLoaded);
	}

	@Test
	public void classPathToClassNameReturnsClassNameCorrectly() {

		final String expectedClassToBeLoaded = "com.ericsson.nms.mediation.cftpluginreader.PluginJarReaderStubbed";
		final String inputClassName = "com/ericsson/nms/mediation/cftpluginreader/PluginJarReaderStubbed.class";
		final String className = pluginReader
				.classPathToClassName(inputClassName);
		assertEquals(expectedClassToBeLoaded, className);
	}

	@Test
	public void createInstanceOfClassUsingConstructorReturningTheInstanceCorrectly()
			throws Exception {
		final String classToBeLoaded = "com.ericsson.nms.mediation.cftpluginreader.PluginJarReaderStubbed";
		final Object object = pluginReader
				.createInstanceOfClassUsingConstructor(classToBeLoaded);
		assertNotNull("Object is not created", object);
	}

	@Test
	public void createInstanceOfClassUsingConstructorReturningTheInstanceCorrectlyEvenIfClassNameIsGivenAsClassPath()
			throws Exception {
		final String classToBeLoaded = "com/ericsson/nms/mediation/cftpluginreader/PluginJarReaderStubbed.class";
		final Object object = pluginReader
				.createInstanceOfClassUsingConstructor(classToBeLoaded);
		assertNotNull("Object is not created", object);
	}
	
	@Test
	public void createInstanceOfDifferntPluginClassPresentAtDifferntDirectoryPaths()
			throws Exception {
		final String pluginClass = "com.ericsson.nms.umts.ranos.pms.collectionplugins.gpeh.GpehJobPlugin";
		final Object plugin = pluginReader
				.createInstanceOfClassUsingConstructor(pluginClass);
		assertNotNull("Object is not created", plugin);
		String differntJarPath = userDir + JAR_FILE_PATH+"plugintest"+"/";
		pluginReader = PluginJarReader.getPluginReaderInstance(differntJarPath);
		final String nonPMSPluginClass = "nonpmsplugin.NonPMSPluginStub";
		final Object nonPMSPlugin = pluginReader
				.createInstanceOfClassUsingConstructor(nonPMSPluginClass);
		assertNotNull("Object is not created", nonPMSPlugin);
	}
	
	@Test(expected=CustomCftException.class)
	public void providingDifferntPathReturnDifferentPluginReaderAndInstantiateOnlyAssociatedPathLoadedClasses()
			throws Exception {
		final String pluginClass = "com.ericsson.nms.umts.ranos.pms.collectionplugins.gpeh.GpehJobPlugin";
		final Object plugin = pluginReader
				.createInstanceOfClassUsingConstructor(pluginClass);
		assertNotNull("Object is not created", plugin);
		pluginReader = PluginJarReader.getPluginReaderInstance(userDir + JAR_FILE_PATH+"plugintest/");
		final String nonPMSPluginClass = "nonpmsplugin.NonPMSPluginStub";
		final Object nonPMSPlugin = pluginReader
				.createInstanceOfClassUsingConstructor(nonPMSPluginClass);
		assertNotNull("Object is not created", nonPMSPlugin);
		pluginReader.createInstanceOfClassUsingConstructor(pluginClass);
	}
	
}
