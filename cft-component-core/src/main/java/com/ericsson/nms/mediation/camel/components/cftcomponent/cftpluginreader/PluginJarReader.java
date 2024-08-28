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
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTJarFilter;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CustomCftException;

/**
 * This class will instantiate the plug-in jar loader & create the instance of
 * the class from those jars based on the class name
 * 
 * @author esersla
 * 
 */

public class PluginJarReader {

    private static final transient Logger LOG = LoggerFactory
            .getLogger(PluginJarReader.class);

    /**
     * loader which creates the instance of plug-in class
     */
    private transient JarFileLoader loader;

    /**
     * for storing the reference of jar files read from the plug-in directory
     */
    private File jar_files[] = {};

    private static boolean isPluginJarsLoaded = false;

    /**
     * cache to hold the plug-in jar reader instance for each jar directory
     */
    private static Map<String, PluginJarReader> loadedPluginCache = null;

    private static final String CLASS_FILE_EXT = ".class";

    /**
     * 
     * @param jarPath
     * @return
     * @throws CustomCftException
     */
    public static PluginJarReader getPluginReaderInstance(final String jarPath)
            throws CustomCftException {
        PluginJarReader jarReaderInstance = null;
        isPluginJarsLoaded = false;
        if (loadedPluginCache == null) {
            if (System.getProperty("uetraceCfgFile") == null) {
                System.setProperty("uetraceCfgFile",
                        "/var/opt/ericsson/nms_umts_pms_seg/segment1/UeTraceInfo.txt");
            }
            LOG.debug(" Creating Plugin reader cache ");
            loadedPluginCache = new Hashtable<String, PluginJarReader>();
            jarReaderInstance = new PluginJarReader(jarPath);
            isPluginJarsLoaded = jarReaderInstance.load();
            addReaderToCache(jarPath, jarReaderInstance);
        } else {
            if (loadedPluginCache.get(jarPath) == null) {
                jarReaderInstance = new PluginJarReader(jarPath);
                isPluginJarsLoaded = jarReaderInstance.load();
                addReaderToCache(jarPath, jarReaderInstance);
            } else {
                jarReaderInstance = getReaderFromCache(jarPath);
                isPluginJarsLoaded = true;
            }
        }
        if (!isPluginJarsLoaded) {
            LOG.error(" Jar Loading failed for path " + jarPath);
        }
        return jarReaderInstance;
    }

    /**
     * Constructor for the PluginJarReader, called every time for a new path
     * 
     * @param filePath
     * @throws CustomCftException
     */
    protected PluginJarReader(final String filePath) throws CustomCftException {
        LOG.debug(" Reading the directory " + filePath);
        checkAndListPluginJarDirectory(filePath);
    }

    /**
     * To get instance of PluginJarReader corresponding to jarPath from
     * loadedPluginCache
     * 
     * @param jarPath
     *            key entry to search for in loadedPluginCache
     * @return instance of PluginJarReader
     */
    private static PluginJarReader getReaderFromCache(final String jarPath) {
        PluginJarReader jarReaderInstance;
        jarReaderInstance = loadedPluginCache.get(jarPath);
        return jarReaderInstance;
    }

    /**
     * Adds the new instance of PluginJarReader in cache
     * 
     * @param jarPath
     *            key for adding the new entry
     * @param jarReaderInstance
     */
    private static void addReaderToCache(final String jarPath,
            final PluginJarReader jarReaderInstance) {
        loadedPluginCache.put(jarPath, jarReaderInstance);
    }

    /**
     * @param filePath
     * @throws CustomCftException
     *             is thrown when invalid directory path is provided as
     *             parameter
     */
    private void checkAndListPluginJarDirectory(final String filePath)
            throws CustomCftException {
        final File jarDirectory = new File(filePath);
        if (jarDirectory.exists() && jarDirectory.isDirectory()) {
            final FilenameFilter jarFilter = new CFTJarFilter();
            jar_files = jarDirectory.listFiles(jarFilter);
        } else {
            throw new CustomCftException(-1, " invalid directory path ");
        }
    }

    /**
     * 
     * @return true if jars successfully loaded otherwise false
     * @throws CustomCftException
     *             throws when the URL for the jars provided is incorrect
     */
    protected boolean load() throws CustomCftException {
        boolean status = true;
        if (loader == null) {
            try {
                createClassLoader();
            } catch (final MalformedURLException e) {
                LOG.error(" Incorrect URL provided for Plugin Jars ", e);
                status = false;
                throw new CustomCftException(-1,
                        " Incorrect URL provided for Plugin Jars " + e);
            }
        }
        return status;
    }

    /**
     * to load all the jars in jar path
     * 
     * @throws MalformedURLException
     *             - thrown when incorrect URL path provided for the jars
     */
    private void createClassLoader() throws MalformedURLException {
        // Load the jar file first.
        LOG.debug(" loading the jars ");
        loader = new JarFileLoader(jar_files, Thread.currentThread()
                .getContextClassLoader());
        LOG.debug(" successfully loaded all the jars ");
    }

    /**
     * /** This method will attempt to create an instance of the class name
     * passed to it
     * 
     * @param nameOfClassToLoad
     *            the class to be loaded e.g. com.ericsson.nms.mediation.MyClass
     *            or com/ericsson/nms/m/MyClass.class
     * 
     * @param nameOfClassToLoad
     * @return the created object instance
     * @throws CustomCftException
     *             thrown in case of ClassNotFound Exception, NoSuchMethod
     *             Exception, Security Exception, IllegalAccess Exception,
     *             IllegalArgument Exception
     */
    public Object createInstanceOfClassUsingConstructor(
            final String nameOfClassToLoad) throws CustomCftException {
        // Simple check here. Its possible for the class to be specified as
        // either a path into the jar file or as a class. We'll just convert it
        // for them.
        String className = nameOfClassToLoad;

        if (loader == null) {
            LOG.error(" Jar Loader not initialized. Returning null");
            throw new CustomCftException(-1, " Failed to create instance of: "
                    + className);
        }

        if (className.indexOf('/') > 0) {
            className = classPathToClassName(className);
        }
        try {
            Class<?> loadingClass;
            loadingClass = loader.loadClass(className);

            final Constructor<?> classConstructor = loadingClass
                    .getConstructor();
            return classConstructor.newInstance();
        } catch (Exception e) {
            LOG.error(" Failed to create instance of: {} due to {}",
                    new Object[] { className, e });
            throw new CustomCftException(-1, " Failed to create instance of: "
                    + className + " due to " + e);
        }
    }

    /**
     * sanity check for class name
     * 
     * @param classPath
     * @return
     */
    protected String classPathToClassName(final String classPath) {
        String result = classPath;
        if (classPath.indexOf(CLASS_FILE_EXT) > 0) {
            result = classPath.substring(0, classPath.lastIndexOf('.'));
        }
        // Remove the .class from the end.
        // Replace all the "/" with "." turning it from a path into
        // a class string.
        result = result.replace('/', '.');
        return result;
    }

}
