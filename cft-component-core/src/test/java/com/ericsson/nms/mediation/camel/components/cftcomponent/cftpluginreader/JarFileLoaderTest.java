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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.junit.*;

import com.ericsson.nms.mediation.camel.components.cftcomponent.cftpluginreader.JarFileLoader;

public class JarFileLoaderTest {
	
	JarFileLoader jarFileLoader = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}

	/**
	 * Test method for {@link com.ericsson.nms.mediation.camel.components.cftcomponent.cftpluginreader.JarFileLoader#JarFileLoader(java.net.URL[])}.
	 * @throws MalformedURLException 
	 */
	@Test
	public void testJarFileLoader() throws MalformedURLException {
		String userDir = System.getProperty("user.dir");
		jarFileLoader = new JarFileLoader(new File[]{new File(userDir+"/src/test/resources/common_plugin.jar"),new File(userDir+"/src/test/resources/pms_ftpm_plugins.jar")}, jarFileLoader);
		assertNotNull(jarFileLoader);
	}

	/**
	 * Test method for {@link com.ericsson.nms.mediation.camel.components.cftcomponent.cftpluginreader.JarFileLoader#addFile(java.lang.String)}.
	 * @throws MalformedURLException 
	 */
	@Test
	public void testAddFile() throws MalformedURLException {
		
		String userDir = System.getProperty("user.dir");
		
		final String JAR_FILE_PATH = "/src/test/resources/testplugin.jar";
		
		jarFileLoader = new JarFileLoader(new File[]{new File(userDir+JAR_FILE_PATH)}, null); 
		
		URL [] urls = jarFileLoader.getURLs() ;
		
		URL expectedUrl = new URL("jar:file:"+"///"+userDir+JAR_FILE_PATH+"!/");
		
		final Set<URL> VALUES = new HashSet<URL>(Arrays.asList(urls));
		
		assertTrue(VALUES.contains(expectedUrl));
		
	}

}
