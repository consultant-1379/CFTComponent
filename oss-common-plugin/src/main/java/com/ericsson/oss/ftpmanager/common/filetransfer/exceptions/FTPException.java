/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.common.filetransfer.exceptions;

import com.ericsson.oss.ftpmanager.common.constants.StringConstants;
import com.ericsson.oss.ftpmanager.common.logging.FTPManagerError;

/**
 * @author kamkaka
 * 
 */
public abstract class FTPException extends Exception { //NOPMD

    private static final long serialVersionUID = 1L;

    private final FTPErrorArea fTPErrorArea; //NOPMD

    private String labelForStatistics = null; //NOPMD   

    public static final FTPManagerError NO_FTPM_ERROR_KEY = null;

    private int errorCode = 0;

    public int getErrorCode() {
        return errorCode;
    }

    public int getFTPErrorCode() {
        return -1;
    }

    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Constructor
     * 
     * @param fTPErrorArea
     *            Error Area for this exception
     * @param message
     *            Message for this exception
     */
    public FTPException(final FTPErrorArea fTPErrorArea, final String message) {
        super(message);
        this.fTPErrorArea = fTPErrorArea;
    }

    public FTPException(final Throwable exception,
            final FTPErrorArea fTPErrorArea, final String msg) {
        super(msg, exception);
        this.fTPErrorArea = fTPErrorArea;
    }

    public String getLabelForStatistics() {
        if (labelForStatistics == null) {
            final StringBuilder builder = new StringBuilder();
            builder.append(fTPErrorArea.toString());
            builder.append(StringConstants.SPACE);
            builder.append(errorCode);
            builder.append(StringConstants.SPACE);
            builder.append(getClass().getSimpleName());
            labelForStatistics = builder.toString();
        }
        return labelForStatistics;
    }

    public FTPManagerError getFTPManagerError() {
        // Be default PMSError Key i.e. No logging... Will need to override where applicable
        return NO_FTPM_ERROR_KEY;
    }

    public static abstract class NodeException extends FTPException { //NOPMD
        public NodeException(final FTPErrorArea fTPErrorArea, final String msg) {
            super(fTPErrorArea, msg);
        }

        public NodeException(final Throwable exception,
                final FTPErrorArea fTPErrorArea, final String msg) {
            super(exception, fTPErrorArea, msg);
        }
    }

    public static abstract class SystemException extends FTPException { //NOPMD
        public SystemException(final FTPErrorArea fTPErrorArea, final String msg) {
            super(fTPErrorArea, msg);
        }
    }

    public static abstract class FileException extends FTPException { //NOPMD
        public FileException(final FTPErrorArea fTPErrorArea, final String msg) {
            super(fTPErrorArea, msg);
        }
    }

    //  N O D E   L E V E L   E X C E P T I O N S

    /**
     * The remote server refused the ftp connection
     */
    public static class ConnectionException extends NodeException { //NOPMD
        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param message
         *            Message for this exception
         */
        public ConnectionException(final FTPErrorArea fTPErrorArea,
                final String message) {
            super(fTPErrorArea, message);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.NE_FTP_FAILURE;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.NODE_CONNECTION_EXCEPTION;
        }

        public ConnectionException(final Throwable exception,
                final FTPErrorArea fTPErrorArea, final String msg) {
            super(exception, fTPErrorArea, msg);
        }

    }

    /**
     * The remote server refused the ftp connection
     */
    public static class DirectoryListingException extends NodeException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public DirectoryListingException(final FTPErrorArea fTPErrorArea,
                final String msg) {
            super(fTPErrorArea, msg);
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.NODE_DIRECTORY_LISTING_EXCEPTION;
        }
    }

    //  S Y S T E M   L E V E L   E X C E P T I O N S
    /**
     * There was no write permission to create a OSS file/dir
     */
    public static class WritePermissionException extends SystemException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public WritePermissionException(final String msg) {
            super(FTPErrorArea.FILE, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.SEGMENT_WRITE_ACCESS_DENIED;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.SYSTEM_WRITE_PERMISSIN_EXCEPTION;
        }
    }

    /**
     * Exception Raised if the local volume is full and a write failed.
     * 
     * @note This is <i>difficult</i> to achieve in Java since the free space on
     *       a volume is not available through an API and there is no specific
     *       exception for this case. The assumption we make is that if
     *       permissions are ok then a write error means the volume is full
     */
    public static class VolumeFullException extends SystemException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public VolumeFullException(final String msg) {
            super(FTPErrorArea.FILE, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.SEGMENT_VOLUME_FULL;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.SYSTEM_VOLUME_FULL_EXCEPTION;
        }

    }

    /**
     * For wrapping FTP-Syntax exceptions , these should really never occur
     * 
     * @author eeitsik
     */
    public static class CommandSyntaxException extends SystemException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public CommandSyntaxException(final FTPErrorArea fTPErrorArea,
                final String msg) {
            super(fTPErrorArea, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.NE_FTP_FAILURE;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.SYSTYEM_COMMAND_SYNTAX_EXCEPTION;
        }

    }

    //   F I L E   L E V E L   E X C E P T I O N S

    /**
     * Raised if the target file already exists
     */
    public static class AlreadyExistsException extends FileException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public AlreadyExistsException(final String msg) {
            super(FTPErrorArea.FILE, msg);
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.FILE_FILE_ALREADY_EXIST_EXCEPTION;
        }
    }

    /**
     * The unzip stream failed
     */
    public static class UnzipStreamException extends FileException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public UnzipStreamException(final FTPErrorArea fTPErrorArea,
                final String msg) {
            super(fTPErrorArea, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.NE_FTP_FAILURE;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.FILE_UNZIPPED_STREAM_EXCEPTION;
        }
    }

    /**
     * The file was empty
     */
    public static class FileEmptyException extends FileException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public FileEmptyException(final String msg) {
            super(FTPErrorArea.FILE, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.NE_FTP_FAILURE;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.FILE_FILE_EMPTY_EXCEPTION;
        }

    }

    /**
     * General exception if an error occurred during setup/transfer
     */
    public static class FileTransferException extends FileException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public FileTransferException(final FTPErrorArea fTPErrorArea,
                final String msg) {
            super(fTPErrorArea, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.NE_FTP_FAILURE;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.FILE_FILE_TRANSFER_EXCEPTION;
        }

    }

    /**
     * Exception Raised if the size of a file to be transfered from a host
     * exceeds the limits specified in the segment parameters. For example, if
     * gpehMaxFileSize is exceeded the file will not be transfered from the
     * host.
     */
    public static class FileSizeExceedsLimits extends FileException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public FileSizeExceedsLimits(final String msg) {
            super(FTPErrorArea.FILE, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.NE_FTP_FAILURE;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.FILE_FILE_SIZE_EXCEEDS_LIMIT_EXCEPTION;
        }
    }

    /**
     * General exception if an error occurred during setup/transfer with a
     * unknown/unexpected ftp error code
     */
    public static class UnknownFtpErrorCodeException extends FileException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public UnknownFtpErrorCodeException(final FTPErrorArea fTPErrorArea,
                final String msg) {
            super(fTPErrorArea, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.NE_FTP_FAILURE;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.FILE_UNKNWON_FILE_TRANSFER_EXCEPTION;
        }
    }

    /**
     * The file was not available on the NE Probably means the path on the Node
     * was incorrect
     */
    public static class FileNotAvailableException extends NodeException { //NOPMD

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param msg
         *            Message for this exception
         */
        public FileNotAvailableException(final FTPErrorArea fTPErrorArea,
                final String msg) {
            super(fTPErrorArea, msg);
        }

        @Override
        public FTPManagerError getFTPManagerError() {
            return FTPManagerError.REMOTE_FILE_NOT_FOUND_ERROR;
        }

        @Override
        public int getFTPErrorCode() {
            return FileTransferErrorCodes.FILE_FILE_NOT_AVAILABLE_EXCEPTION;
        }
    }

}
