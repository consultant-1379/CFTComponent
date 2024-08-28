/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import com.ericsson.oss.ftpmanager.common.constants.StringConstants;
import com.ericsson.oss.ftpmanager.common.utilities.FileUtilities;

/**
 * This class contains the file transfer details of the file that is to be
 * transfered. Application that wants to collect/transfer the file will populate
 * the fields of this class and send it to file transfer handler.
 * 
 * @author kamkaka
 * 
 */
public class SingleFileTransferInfo implements Serializable { //NOPMD

    public static final long serialVersionUID = -9098142964463721560L;

    private static final String NEW_LINE_AND_OSS_FILE_DETAILS = "       OSS file details: ";

    private static final String NODE_FILE_DETAILS = "   Node file details: ";

    /**
     * Application will create this id for post transfer activities Once file is
     * transfered File Transfer handler will send a call back to application
     * with this id, so that application can retrieve respective job and perform
     * the post processing accordingly.
     * 
     */
    public String transfer_id;

    public String node_fdn;

    public String fth_info;

    public boolean isCreatedWithInterceptor;

    /**
     * File name which the file will be stored on OSS with.
     */
    public String oss_file_name;

    /**
     * Node file name that is to be collected
     */
    public String node_file_name;

    /**
     * OSS path where file has to be collected.
     */
    public String oss_path;

    /**
     * Node path where file is available for collection
     */
    public String node_path;

    /**
     * Plugin to be applied
     */
    public String plugin_class_name;

    /**
     * Node Info which the file is to be collected from
     */
    public NodeInfo nodeInfo;

    /**
     * Application can set maximum file size to be collected.
     */
    public int maxFileSize;

    /**
     * On the node all the files are generated in compresses format (.gz), so
     * application will set this to tell file transfer handler if the file has
     * to collected as uncompressed.
     */
    public boolean uncompressed_needed;

    //Has the file been transfered successfully from the node to the OSS-RC
    public boolean transferSucceeded = false;

    //Time file collection started
    public long startTime;

    //Time file collection ended
    public long endTime;

    //ROP end time of a job on a node
    public long ropEndTime;

    // Collection period for a job
    public long collectionPeriod;

    //Total number of bytes transferred from node
    public long bytesTransferred;

    //Total number of bytes received from node.
    //If this file isn't uncompressed by PMS, then this will be same as bytesTransferred
    public long bytesStored;

    //String details to be used for Error Logging
    private String detailsForErrorLogging;

    public int errorCode;

    public String errorMessage;

    public int priority;

    public long interceptionStartTime;

    public long interceptionEndtime;

    public int nodeType;

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime() {
        this.startTime = getCurrentTime();
    }

    public long getEndTime() {
        return endTime;
    }

    public void postFileCollectionActions() {
        endTime = getCurrentTime();
        updateBytesStored();
    }

    /**
     * @return current time as long
     */
    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Update bytes stored, normally the same as bytes transferred but if unzip
     * is done it will need to check the file-size of the un-zipped file.
     */
    private void updateBytesStored() {
        if (uncompressed_needed) {
            bytesStored = getFileSize();
        } else {
            // When no unzip is required bytes stored is same as bytesTransferred
            bytesStored = bytesTransferred;
        }

    }

    /**
     * @return
     */
    private long getFileSize() {
        return FileUtilities.getFileSize(oss_path, oss_file_name);
    }

    /**
     * Creates a string of information that is used for logging if the file
     * collection fails
     * 
     * @return String
     */
    public String getDetailsForErrorLogging() {
        if (detailsForErrorLogging == null) {
            final StringBuilder stringBuilder = createBasicDetailsForErrorLogging();
            detailsForErrorLogging = stringBuilder.toString();
        }
        return detailsForErrorLogging;
    }

    private StringBuilder createBasicDetailsForErrorLogging() {
        final StringBuilder builder = new StringBuilder();
        builder.append(NODE_FILE_DETAILS);
        builder.append(node_path);
        builder.append(File.separatorChar);
        builder.append(node_file_name);
        builder.append(NEW_LINE_AND_OSS_FILE_DETAILS);
        builder.append(oss_path);
        builder.append(File.separatorChar);
        builder.append(oss_file_name);
        return builder;
    }

    public String getNodeFullFileName() {
        String nodeFullFileName = node_file_name;
        if (node_path != null) {
            nodeFullFileName = node_path + File.separator + node_file_name;
        }
        return nodeFullFileName /*
                                 * node_path != null ? node_path +
                                 * File.separator + node_file_name :
                                 * node_file_name
                                 */;
    }

    public String getOssFullFileName() {
        String ossFullFileName = oss_file_name;
        if (oss_path != null) {
            ossFullFileName = oss_path + File.separator + oss_file_name;
        }
        return ossFullFileName/*
                               * oss_path != null ? oss_path + File.separator +
                               * oss_file_name : oss_file_name
                               */;
    }

    public String getPluginClass_name() {
        return plugin_class_name;
    }

    @Override
    public boolean equals(final Object obj) {
        boolean result = false;
        if ((this.transfer_id != null && ((SingleFileTransferInfo) obj).transfer_id != null)
                && this.transfer_id
                        .equalsIgnoreCase(((SingleFileTransferInfo) obj).transfer_id)) {
            result = true;
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder logMessage = new StringBuilder();
        final Date ropTime = new Date(ropEndTime);
        logMessage.append("FDN:" + node_fdn);
        logMessage.append(StringConstants.PIPE);
        logMessage.append("RET:" + ropTime);
        logMessage.append(StringConstants.PIPE);
        logMessage.append("OFN:" + oss_file_name);
        logMessage.append(StringConstants.PIPE);
        logMessage.append("NFN:" + node_file_name);
        logMessage.append(StringConstants.PIPE);
        logMessage.append("OFP:" + oss_path);
        logMessage.append(StringConstants.PIPE);
        logMessage.append("NFP:" + node_path);
        logMessage.append(StringConstants.PIPE);
        return logMessage.toString();
    }

}
