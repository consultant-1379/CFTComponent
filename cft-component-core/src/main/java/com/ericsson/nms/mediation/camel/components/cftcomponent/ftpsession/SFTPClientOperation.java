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
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.CFTUtils;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class SFTPClientOperation {

    private static final transient Logger LOG = LoggerFactory
            .getLogger(SFTPClientOperation.class);

    /**
     * This method is used for performing the directory listing using SSH FTP
     * 
     * @param dirname
     *            the remote path on the network element on which of listing
     *            directory has to be done
     * @param filenameFilter
     *            the filter to be used once listing is done to get the relevant
     *            files
     * @param channel
     * @return fileList a list of source file names that need to be collected
     * @throws CompositeFTPException
     *             to bundle any FTP exception
     */
    public Collection<String> useSftpListing(final String dirname,
            final String[] filenameFilter, final ChannelSftp channel)
            throws CompositeFTPException {
        final Collection<String> fileList = new ArrayList<String>();
        try {
            @SuppressWarnings("unchecked")
            final Vector<ChannelSftp.LsEntry> listedFiles = channel.ls(dirname);

            for (final ChannelSftp.LsEntry sftpFile : listedFiles) {
                if (CFTUtils.isFileInFilter(sftpFile.getFilename(),
                        filenameFilter)) {
                    fileList.add(sftpFile.getFilename());
                }
            }
        } catch (SftpException sftpException) {

            final String errorMsg = "An SftpException "
                    + sftpException.getLocalizedMessage()
                    + " occured while useSftpListing() of directory { "
                    + dirname + "} to an output stream";

            LOG.error(" Exception occured while useSftpCollect() is {}",
                    sftpException);
            throw new CompositeFTPException(
                    CFTUtils.extractSftpErrorCodeException(sftpException),
                    errorMsg);

        } catch (final Exception exception) {
            LOG.error(" Exception occured while useSftpListing() is {}",
                    exception);
            throw new CompositeFTPException(exception.getCause().toString());
        }
        return fileList;
    }

    /**
     * To transfer the file from source to destination using SSH-FTP
     * 
     * @param singlefiletransferinfo
     *            file collection object containing source & destination paths
     * @param channel
     * @return true on successful transfer otherwise false
     * @throws CompositeFTPException
     *             if any SftpException occurs, it will be bundled into custom
     *             exception
     * @throws NumberFormatException
     */
    public boolean useSftpCollect(
            final SingleFileTransferInfo singlefiletransferinfo,
            final ChannelSftp channel) throws CompositeFTPException {
        FileOutputStream fileOutputStream = null;
        File destfile = null;
        try {

            LOG.debug(" going to fetch the node file {} to oss at location {}",
                    new Object[] {
                            singlefiletransferinfo.getNodeFullFileName(),
                            singlefiletransferinfo.getOssFullFileName() });

            destfile = new File(singlefiletransferinfo.getOssFullFileName());
            fileOutputStream = new FileOutputStream(destfile);

            channel.get(singlefiletransferinfo.getNodeFullFileName(),
                    fileOutputStream);

            LOG.debug("{} file successfully collected for post processor",
                    singlefiletransferinfo.oss_file_name);
            return true;
        } catch (final SftpException sftpException) {

            final String errorMsg = "An SftpException "
                    + sftpException.getLocalizedMessage()
                    + " occured trying to download the file { "
                    + singlefiletransferinfo.getNodeFullFileName()
                    + "} to an output stream";

            LOG.error(" Exception occured while useSftpCollect() is {}",
                    sftpException);
            deleteEmptyFile(destfile);
            throw new CompositeFTPException(
                    CFTUtils.extractSftpErrorCodeException(sftpException),
                    errorMsg);

        } catch (final Exception exception) {
            LOG.error(" Exception occured while useSftpCollect() is {}",
                    exception);
            deleteEmptyFile(destfile);
            throw new CompositeFTPException(exception.getCause().toString());
        } finally {
            if (fileOutputStream != null) {
                try {

                    fileOutputStream.close();
                } catch (IOException e) {
                    LOG.error(
                            " Exception occured while closing output stream is {}",
                            e);
                }
            }
        }
    }

    /**
     * deletes the empty file
     * 
     * @param destfile
     *            file to delete on local file system
     */
    private void deleteEmptyFile(final File destfile) {
        if (destfile != null) {
            destfile.delete();
        }
    }
}
