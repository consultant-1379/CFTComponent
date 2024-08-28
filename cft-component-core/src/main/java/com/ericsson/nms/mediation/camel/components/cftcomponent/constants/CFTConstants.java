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
package com.ericsson.nms.mediation.camel.components.cftcomponent.constants;

public class CFTConstants {
    public static final String CLASS_NAME = "ftPluginClassname";
    public static final String JAR_SOURCE_DIRECTORY = "ftPluginJarDirectory";
    public static final String CFT_FILE_LIST = "compositeFileList";

    public static final String CFT_SOURCE_FILE = "srcFile";
    public static final String CFT_DESTINATION_FILE = "destFile";
    public static final String CFT_SOURCE_DIRECTORY = "srcDir";
    public static final String CFT_DESTINATION_DIRECTORY = "destDir";
    public static final String CFT_TARGET_IP_ADDRESS = "ipAddress";
    public static final String CFT_TARGET_PORT = "port";
    public static final String CFT_SECURE_FTP = "secureFtp";
    public static final String CFT_TARGET_USERNAME = "username";
    public static final String CFT_TARGET_PASSWORD = "password";
    public static final String CFT_CONNECTION_KEY = "connectionKey";
    public static final String CFT_CLIENT = "connectionClient";
    public static final String PLUGIN_NAME = "plugin_name";
    public static final String FC_JOB_JOB_ID = "FileCollection_job_id";
    public static final String DECOMPRESSION_REQUIRED = "decompression_required";
    public static final String TIME_ZONE = "timezone";

    /**
     * JSchException error messages
     */
    public enum JSchErrorMessages {

        CONNECTION_TIMED_OUT(9, "Connection timed out",
                "incorrect ip address for remote host"),

        INVALID_ADD(9, "Address is invalid",
                "incorrect ip address/port for remote host"),

        UNKNOWN_HOST_EXCEPTION(9, "UnknownHostException",
                "remote host not avaialble for sftp connection"),

        USER_NAME_NOT_NULL(0, "username must not be null",
                "sftp login username in not valid or null"),

        SESSION_DOWN(0, "session is down",
                "remote host not avaialble for sftp connection"),

        CONNECTION_REFUSED(0, "Connection refused",
                "remote host not avaialble for sftp connection"),

        READ_TIMED_OUT(0, "Read timed out",
                "ftpclient/server comunication timed out"),

        AUTH_FAIL(0, "Auth fail", "sftp login username/password incorrect"),

        /**
         * This will be used only when Exception caused in
         * <code>ConnectionPool's</code> <code>borrowObject()</code> is not
         * <code>JSchException</code>
         */
        UNKNOWN_EXCEPTION(8, "N/A", "N/A");

        /**
         * this is the error code which will be mapped to PMS error code in
         * FTPErrorProcessor
         */
        private final int errorCode;

        /**
         * this is the message received in JSchException
         */
        private final String jschErrMSg;

        /**
         * message will be sent to PMS along with error code
         */
        private final String replyMsg;

        JSchErrorMessages(final int errorCode, final String errorMsg,
                final String replyMsg) {
            this.errorCode = errorCode;
            this.jschErrMSg = errorMsg;
            this.replyMsg = replyMsg;
        }

        /**
         * @return the errorCode
         */
        public int getErrorCode() {
            return errorCode;
        }

        /**
         * @return the errorMsg
         */
        public String getErrorMsg() {
            return jschErrMSg;
        }

        /**
         * @return the replyMsg
         */
        public String getReplyMsg() {
            return replyMsg;
        }

    }

}
