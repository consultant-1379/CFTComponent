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
package com.ericsson.nms.mediation.camel.components.cftcomponent.utils;

import static com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTConstants.JSchErrorMessages.*;

import org.apache.camel.EndpointConfiguration;
import org.apache.camel.RuntimeCamelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.mediation.camel.components.cftcomponent.CftEndpoint;
import com.ericsson.nms.mediation.camel.components.cftcomponent.cftpluginreader.PluginJarReader;
import com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTConstants;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CustomCftException;
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.FTPMPluginFactoryInterface;
import com.jcraft.jsch.SftpException;

public abstract class CFTUtils {

    private static final transient Logger LOG = LoggerFactory
            .getLogger(CFTUtils.class);

    /**
     * Check if the given filename is 'in' the given filenameFilter returns true
     * if the filename contains at least 1 string from the filenameFilter will
     * also return false if filter is empty
     * 
     * @param fileName
     *            the name of the remote file fetched as part of directory
     *            listing
     * @param filenameFilter
     *            array of filters containing String literals used for taking
     *            out only relevant files for the current composite file
     *            collection job
     * @return true if file name matches one of the filter
     */
    public static boolean isFileInFilter(final String fileName,
            final String[] filenameFilter) {
        for (final String filter : filenameFilter) {
            if (fileName.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given the name of the class, this method will create the instance of it
     * using the JarFileReader & reflection
     * 
     * @param className
     *            the plug-in class name provided in
     *            <code>CompositeFileCollectionJob</code> received in the
     *            exchange by this component
     * 
     * @return object of the class name provided as the String parameter
     * 
     * @throws CustomCftException
     */
    public static FTPMPluginFactoryInterface instantiateClass(
            final String className, final CftEndpoint endpoint)
            throws CustomCftException {
        try {
            if (className != null && !className.isEmpty()) {
                LOG.debug("Instantiating class: {}", className);
                final String jarDirectory = getJarDirectory(endpoint);
                final PluginJarReader pluginJarLoader = PluginJarReader
                        .getPluginReaderInstance(jarDirectory);

                final FTPMPluginFactoryInterface instanceOfClass = (FTPMPluginFactoryInterface) pluginJarLoader
                        .createInstanceOfClassUsingConstructor(className);
                LOG.debug("Loaded Plugin object: {}", instanceOfClass
                        .getClass().getCanonicalName());
                return instanceOfClass;
            } else {
                throw new CustomCftException(-1,
                        "Plugin class name is not valid, not performing any composite operations");
            }

        } catch (final RuntimeCamelException e) {
            LOG.error("RuntimeCamelException: {}", e);
            throw new CustomCftException(-1,
                    "RuntimeCamelException in instantiated class: " + e);
        } catch (final Exception e) {
            LOG.error("Exception: " + e.getMessage());
            throw new CustomCftException(-1, "Exception in instantiate class: "
                    + e);
        }
    }

    /**
     * It gives the Plug-in jar location on OSS RC shared location
     * 
     * @return String directory location
     */
    private static String getJarDirectory(final CftEndpoint endpoint)
            throws RuntimeCamelException {
        String result;
        final EndpointConfiguration epconfig = endpoint
                .getEndpointConfiguration();
        result = epconfig.getParameter(CFTConstants.JAR_SOURCE_DIRECTORY)
                .toString();
        return result;
    }

    /**
     * <p>
     * This method extracts the error code from a JschException. The error code
     * is the first item in the String when calling toString on a JschException
     * and is followed by a colon (:)
     * </p>
     * <p>
     * Example<br>
     * <b>0: Some error message description....</b>
     * </p>
     * 
     * @param exception
     *            - SftpException
     * @return the integer error code
     */
    public static int extractSftpErrorCodeException(
            final SftpException exception) {
        final String errorMessage = exception.toString();
        final String errorCodeString = errorMessage.substring(0,
                errorMessage.indexOf(":")).trim();
        return Integer.parseInt(errorCodeString);
    }

    /**
     * @param jschException
     * @throws GenericEftpException
     */
    public static void resolveJschErrorCodes(final Exception jschException)
            throws CompositeFTPException {
        String error_message = jschException.getMessage();

        // this error code will be returned only in case of non JSchException
        int errorCode = UNKNOWN_EXCEPTION.getErrorCode();

        if (error_message.contains(CONNECTION_TIMED_OUT.getErrorMsg())) {
            errorCode = CONNECTION_TIMED_OUT.getErrorCode();
            error_message = CONNECTION_TIMED_OUT.getReplyMsg();

        } else if (error_message.contains(INVALID_ADD.getErrorMsg())) {
            errorCode = INVALID_ADD.getErrorCode();
            error_message = INVALID_ADD.getReplyMsg();

        } else if (error_message.contains(UNKNOWN_HOST_EXCEPTION.getErrorMsg())) {
            errorCode = UNKNOWN_HOST_EXCEPTION.getErrorCode();
            error_message = UNKNOWN_HOST_EXCEPTION.getReplyMsg();

        } else if (error_message.contains(USER_NAME_NOT_NULL.getErrorMsg())) {
            errorCode = USER_NAME_NOT_NULL.getErrorCode();
            error_message = USER_NAME_NOT_NULL.getReplyMsg();

        } else if (error_message.contains(SESSION_DOWN.getErrorMsg())) {
            errorCode = SESSION_DOWN.getErrorCode();
            error_message = SESSION_DOWN.getReplyMsg();

        } else if (error_message.contains(CONNECTION_REFUSED.getErrorMsg())) {
            errorCode = CONNECTION_REFUSED.getErrorCode();
            error_message = CONNECTION_REFUSED.getReplyMsg();

        } else if (error_message.contains(READ_TIMED_OUT.getErrorMsg())) {
            errorCode = READ_TIMED_OUT.getErrorCode();
            error_message = READ_TIMED_OUT.getReplyMsg();

        } else if (error_message.contains(AUTH_FAIL.getErrorMsg())) {
            errorCode = AUTH_FAIL.getErrorCode();
            error_message = AUTH_FAIL.getReplyMsg();
        }

        throw new CompositeFTPException(errorCode,
                "Connection could not be established with Network Element due to "
                        + error_message);
    }

}
