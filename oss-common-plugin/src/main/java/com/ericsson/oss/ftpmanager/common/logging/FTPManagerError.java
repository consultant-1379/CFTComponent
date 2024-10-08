/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.common.logging;

/**
 * @author kamkaka
 * 
 */
public enum FTPManagerError {

    COLLECT_DATA_TASK_TIMEOUT_ERROR(
            "COM.ERICSSON.OSS.FTPMANAGER.COLLECT_DATA_TASK_TIMEOUT_ERROR"),

    CONTROLLER_REGISTRATION_ERROR(
            "COM.ERICSSON.OSS.FTPMANAGER.CONTROLLER_REGISTRATION_ERROR"),

    CONTROLLER_NOT_STARTED(
            "COM.ERICSSON.OSS.FTPMANAGER.CONTROLLER_NOT_STARTED_ERROR"),

    TASK_GROUP_EXECUTION_ERROR(
            "COM.ERICSSON.OSS.FTPMANAGER.TASK_GROUP_EXECUTION_ERROR"),

    THREAD_EXECUTION_ERROR("COM.ERICSSON.OSS.FTPMANAGER.THREAD_EXECUTION_ERROR"),

    OBJECT_CREATION_ERROR("COM.ERICSSON.OSS.FTPMANAGER.OBJECT_CREATION_ERROR"),

    CALL_BACK_OBJECT_IS_NULL("COM.ERICSSON.FTPMANAGER.CALL_BACK_OBJECT_IS_NULL"),

    FILE_TRANSFER_OPERATION_PROXY_CREATION_ERROR(
            "COM.ERICSSON.FTPMANAGER.FILE_TRANSFER_OPERATION_PROXY_CREATION_ERROR"),

    //Controller Errors 
    FTP_MANAGER_APPLICATION_REGISTRATION_FAILURE(
            "COM.ERICSSON.FTPMANAGER.CONTROLLER.APPLICATION_REGISTRATION_FAILURE"),

    FTP_MANAGER_FTH_REGISTRATION_FAILURE(
            "COM.ERICSSON.FTPMANAGER.CONTROLLER.FTH_REGISTRATION_FAILURE"),

    FTP_MANAGER_FTH_DEREGISTRATION_FAILURE(
            "COM.ERICSSON.FTPMANAGER.CONTROLLER.FTH_DEREGISTRATION_FAILURE"),

    CONTROLLER_CACHE_LOADING_FAILURE(
            "COM.ERICSSON.FTPMANAGER.CONTROLLER.LOAD_CACHE_ERROR"),

    CONTROLLER_CACHE_PERSISTING_FAILURE(
            "COM.ERICSSON.FTPMANAGER.CONTROLLER.PERSIST_CACHE_ERROR"),

    FTH_CACHE_CALLBACK_LISTENER_OBJECT_NOT_VALID(
            "COM.ERICSSON.FTPMANAGER.CONTROLLER.FTH_CACHE_CALLBACK_LISTENER_OBJECT_NOT_VALID"),

    FTH_INSTANCSE_CACHE_CFG_FILE_NOT_FOUND(
            "COM.ERICSSON.OSS.CONTROLLER.FTH_INSTANCSE_CACHE_CFG_FILE_NOT_FOUND"),

    FTH_INSTANCSE_CACHE_CFG_FILE_WRITE_ERROR(
            "COM.ERICSSON.OSS.CONTROLLER.FTH_INSTANCSE_CACHE_CFG_FILE_WRITE_ERROR"),

    REGISTRATION_SERVICE_CREATION_ERROR(
            "COM.ERICSSON.OSS.FTPMANAGER.CONTROLLER.REGISTRATION_SERVICE_CREATION_ERROR"),

    FILE_NOT_COLLECTED_DUE_TO_PIPE_OVERFLOW(
            "COM.ERICSSON.FTPMANAGER.FILEHANDLER_FILE_NOT_COLLECTED_DUE_TO_PIPE_OVERFLOW"),

    FTP_ERROR("COM.ERICSSON.FTPMANAGER.FILEHANDLER_FTP_ERROR"),

    SEGMENT_WRITE_ACCESS_DENIED(
            "COM.ERICSSON.FTPMANAGER.FILEHANDLER_SEGMENT_WRITE_ACCESS_DENIED"),

    NE_FTP_FAILURE("COM.ERICSSON.FTPMANAGER.FILEHANDLER_NE_FTP_FAILURE"),

    SEGMENT_VOLUME_FULL(
            "COM.ERICSSON.FTPMANAGER.FILEHANDLER_SEGMENT_VOLUME_FULL"),

    CONFIGURATION_ERROR("COM.ERICSSON.COMMON_CONFIGURATION_ERROR"),

    REMOTE_FILE_NOT_FOUND_ERROR(
            "COM.ERICSSON.FTPMANAGER.FILEHANDLER_REMOTE_FILE_NOT_FOUND_ERROR"),

    GENERAL_EXCEPTION("EXCEPTION"),

    PAS_SERVICE_ERROR("COM.ERICSSON.FTPMANAGER.CONTROLLER.PAS_SERVICE_ERROR"),

    FTPMANAGER_WARNING("COM.ERICSSON.FTPMANAGER.WARNING"),

    ADF_MESSAGING_SERVICE_SUBSCRIPTION_FAILED(
            "COM.ERICSSON.FTPMANAGER.ADF_MESSAGING_SERVICE_SUBSCRIPTION_FAILED");

    private final String errorKey;

    private FTPManagerError(final String errorKey) {
        this.errorKey = errorKey;
    }

    public String getErrorKey() {
        return errorKey;
    }

}
