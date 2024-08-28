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
package com.ericsson.nms.mediation.camel.components.cftcomponent.ftpsession;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.CFTUtils;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;

public class FTPClientOperation {

    private static final transient Logger LOG = LoggerFactory
            .getLogger(FTPClientOperation.class);

    /**
     * @param singlefiletransferinfo
     *            file collection job object containing the details of
     *            destination & source file
     * @return true if file transfer is successful otherwise false
     * @throws CompositeFTPException
     *             if any FTP exception occurs while transfer operation then it
     *             will be bundled within CompositeFTPException
     */
    public boolean useFTPCollect(
            final SingleFileTransferInfo singlefiletransferinfo,
            final FTPClient ftpClient) throws CompositeFTPException {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(new File(
                    singlefiletransferinfo.getOssFullFileName()));
            ftpClient.retrieveFile(
                    singlefiletransferinfo.getNodeFullFileName(), fileOut);
            LOG.debug("{} file successfully collected for post processor",
                    singlefiletransferinfo.oss_file_name);
            return true;
        } catch (IOException e) {
            LOG.error(" Exception occured while useFTPCollect() is {}", e);
            throw new CompositeFTPException(ftpClient.getReplyCode(),
                    ftpClient.getReplyString());
        } finally {
            if (fileOut != null) {
                LOG.debug(" All streams successfully closed ? {}",
                        closeDataStreams(fileOut));
                closeDataStreams(fileOut);
            }
        }
    }

    /**
     * This method is used for performing the directory listing using non-secure
     * FTP
     * 
     * @param dirname
     *            the remote path on the network element on which listing of
     *            directory has to be done
     * @param filenameFilter
     *            the filter to be used once listing is done to get the relevant
     *            files
     * @param ftpClient
     * @return fileList a list of source file names that need to be collected
     * @throws CompositeFTPException
     *             to bundle any FTP exception
     */
    public Collection<String> useFTPListing(final String dirname,
            final String[] filenameFilter, final FTPClient ftpClient)
            throws CompositeFTPException {
        final Collection<String> fileList = new ArrayList<String>();
        try {
            LOG.debug("About to call list files on FTP client");

            LOG.debug("Listing files in {}", dirname);

            final FTPFile[] listedFiles = ftpClient.listFiles(dirname);
            for (final FTPFile ftpFile : listedFiles) {
                if (CFTUtils.isFileInFilter(ftpFile.getName(), filenameFilter)) {
                    LOG.debug("adding file to file list {}", ftpFile.getName());
                    fileList.add(ftpFile.getName());
                }
            }
        } catch (IOException e) {
            LOG.error(" Exception occured while useFTPListing() is {}", e);
            throw new CompositeFTPException(ftpClient.getReplyCode(),
                    ftpClient.getReplyString());
        }
        return fileList;
    }

    /**
     * To close the input/output streams after file transfer operation completes
     * 
     * @param streams
     *            streams to be closed after file operation completion
     * @return true if all streams are successfully closed
     */
    protected boolean closeDataStreams(final Closeable... streams) {
        LOG.debug(" closeDataStreams() called");
        boolean closeStatus = true;
        for (final Closeable stream : streams) {
            closeStatus = closeStatus && closeStream(stream);
        }
        return closeStatus;
    }

    /**
     * @param stream
     *            The in/output stream to be closed.
     */
    protected boolean closeStream(final Closeable stream) {
        boolean closeStatus = true;
        if (stream != null) {
            try {
                stream.close();
            } catch (final IOException e) {
                closeStatus = false;
                LOG.error("Error closing FTP stream", e);
            }
        }
        return closeStatus;
    }

}
