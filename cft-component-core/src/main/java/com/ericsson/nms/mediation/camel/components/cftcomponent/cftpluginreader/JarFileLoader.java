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

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This is wrapper class for the URLClassLoader. It has one constructor which
 * takes the URL[] as the parameter. The addFile method helps to append the
 * specified path the the search path for classes.
 * 
 * @author esersla
 * 
 */

public class JarFileLoader extends URLClassLoader {

    public static final String FILE_RESOURCE_LOADER_PATH = "file.resource.loader.path";

    /**
     * 
     * Constructs a new JarFileLoader for the specified URLs using the default
     * delegation of super class. The URLs will be searched in the order
     * specified for classes and resources after first searching in the parent
     * class loader. Any URL that ends with a '/' is assumed to refer to a
     * directory. Otherwise, the URL is assumed to refer to a JAR file which
     * will be downloaded and opened as needed. <br>
     * If there is a security manager, this method first calls the security
     * manager's checkCreateClassLoader method to ensure creation of a class
     * loader is allowed.
     * 
     * @param files
     *            - the initial path for this loader to search.
     * @param classLoader
     * @throws MalformedURLException
     *             - thrown for invalid URL being provided for jars being loaded
     */
    public JarFileLoader(final File[] files, final ClassLoader classLoader)
            throws MalformedURLException {
        super(addFile(files), classLoader);
    }

    /**
     * The path is formated to the format accepted by the URLClassLoader.
     * Appends the specified URL to the list of URLs to search for classes and
     * resources.<br>
     * 
     * @param path
     *            - The complete Jar File Location.
     * @throws MalformedURLException
     *             - thrown for invalid URL being provided for jars being loaded
     */
    private static URL[] addFile(final File[] jarFiles)
            throws MalformedURLException {
        final List<URL> urlList = new ArrayList<URL>();
        final URL urlArray[] = new URL[jarFiles.length];
        for (File jar : jarFiles) {
            final String filePath = "jar:file://" + "/" + jar.getAbsolutePath()
                    + "!/";
            urlList.add(new URL(filePath));
        }
        return urlList.toArray(urlArray);
    }

}
