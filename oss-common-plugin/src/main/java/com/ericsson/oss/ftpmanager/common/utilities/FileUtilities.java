/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.common.utilities;

import java.io.File;

/**
 * @author kamkaka
 * 
 */
public final class FileUtilities {

    private static final String VOBS = "vobs";

    public static final String FTPM_ROOT_FOLDER = "file_handler";

    private static final String JUNIT_TESTFILES_FOLDER = "testfiles"; //NOPMD

    private static final String ECLIPSE = "eclipse"; //NOPMD

    private static final String CC_STORAGE = "cc_storage"; //NOPMD

    //private static String tempFilesPath;

    private FileUtilities() {
        //To make PMD silent
    }

    protected static StringBuilder getRoot() {
        final StringBuilder result = new StringBuilder();
        /*
         * final String userDir = System.getProperty("user.dir"); final String
         * osName = System.getProperty("os.name");
         * 
         * if (osName.startsWith("Windows")) { // windows development
         * environment String viewRoot = userDir.substring(0,
         * userDir.lastIndexOf(FTPM_ROOT_FOLDER)); result.append(viewRoot); }
         * else {
         */
        // In unix we just need to get the bit upto and including \vobs
        //            final int vobsPosition = userDir.indexOf(VOBS);
        //            result.append(userDir.substring(0, vobsPosition));
        //            result.append(VOBS);
        result.append(File.separator);
        result.append(VOBS);
        result.append(File.separator);
        //}
        return result;
    }

    protected static File getFTPManagerVobPath() {
        String root = System.getProperty("user.dir");
        final int posFilehandler = root.lastIndexOf(FTPM_ROOT_FOLDER);
        if (posFilehandler > 0) {
            // Have a user.dir that points to folder somewhere in pms code, so let's use it
            root = root
                    .substring(0, posFilehandler + FTPM_ROOT_FOLDER.length());
        }
        return new File(root);
    }

    protected static File getFTPManagerPackageRoot(final String sourceType) {
        final String root = getFTPManagerVobPath().getPath();
        final String file_handler_package_root = root + File.separator
                + sourceType + File.separator
                + "com" //NOPMD
                + File.separator + "ericsson" + File.separator + "oss"
                + File.separator + "ftpmanager";
        return new File(file_handler_package_root);
    }

    protected static File getTestFTPManagerPackageRoot(final String sourceType) {
        final StringBuilder path = new StringBuilder();
        path.append(getRoot()).append(File.separator).append(FTPM_ROOT_FOLDER);
        path.append(File.separator).append(sourceType).append(File.separator)
                .append("com");
        path.append(File.separator).append("ericsson").append(File.separator)
                .append("oss").append(File.separator).append("ftpmanager");
        return new File(path.toString());
    }

    public static String getTestFilePath() {
        return getTestFTPManagerPackageRoot("junit") + File.separator
                + JUNIT_TESTFILES_FOLDER;
    }

    public static boolean deleteFile(final String path, final String filename) {
        return deleteFile(new File(path, filename));
    }

    public static boolean deleteFile(final String filename) {
        return deleteFile(new File(filename));
    }

    public static boolean deleteFile(final File file) {
        boolean deleteStatus = false;
        if (file != null && file.exists()) {
            deleteStatus = file.delete();
        }
        return deleteStatus;
    }

    public static long getFileSize(final String path, final String filename) {
        final File file = new File(path, filename);
        return getFileSize(file);

    }

    public static long getFileSize(final String fullFilename) {
        final File file = new File(fullFilename);
        return getFileSize(file);

    }

    private static long getFileSize(final File file) {

        // Avoid Excess Exists() calls
        return file.length(); //NOPMD

    }

    /**
     * Returns the full file name of the file in a certain folder. If the passed
     * path is null then the passed file name is used.
     * 
     * @param path
     *            directory where the file is stored this may be null or empty
     *            then the original filename will be returned
     * @param filename
     *            name of file to fetch
     * @return full file name of the file
     */
    public static String calculateFullFilename(final String path,
            final String filename) {
        if ((path == null) || (path.length() == 0)) {
            return filename; //NOPMD
        }
        final StringBuilder builder = new StringBuilder(path.length() + 1
                + filename.length());
        builder.append(path);
        builder.append("/");
        builder.append(filename);
        return builder.toString();
    }
}
