package com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.ftpsessionops;

import java.util.Collection;

import com.ericsson.oss.ftpmanager.common.filetransfer.exceptions.FTPException;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;

/**
 * @author hprasadk
 * 
 */
public interface FTPSessionOperations {
    Collection<String> getDirListing(String dirname,
            final String[] filenameFilter) throws FTPException;

    boolean collectFile(SingleFileTransferInfo fileInfo) throws FTPException;
}
