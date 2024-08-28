/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.common.filetransfer.exceptions;

/**
 * @author kamkaka
 * 
 */
public final class FileTransferErrorCodes {

    private FileTransferErrorCodes() {
        //PMD Silence !!!
    }

    public static final int NODE_CONNECTION_EXCEPTION = 0;

    public static final int NODE_DIRECTORY_LISTING_EXCEPTION = 1;

    public static final int SYSTEM_WRITE_PERMISSIN_EXCEPTION = 2;

    public static final int SYSTEM_VOLUME_FULL_EXCEPTION = 3;

    public static final int SYSTYEM_COMMAND_SYNTAX_EXCEPTION = 4;

    public static final int FILE_FILE_ALREADY_EXIST_EXCEPTION = 5;

    public static final int FILE_UNZIPPED_STREAM_EXCEPTION = 6;

    public static final int FILE_FILE_EMPTY_EXCEPTION = 7;

    public static final int FILE_FILE_TRANSFER_EXCEPTION = 8;

    public static final int FILE_FILE_SIZE_EXCEEDS_LIMIT_EXCEPTION = 9;

    public static final int FILE_FILE_NOT_AVAILABLE_EXCEPTION = 10;

    public static final int FILE_UNKNWON_FILE_TRANSFER_EXCEPTION = 11;

}
