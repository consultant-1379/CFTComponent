package com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface;

import java.util.List;

import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.ftpsessionops.FTPSessionOperations;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;

public interface FTPMPostProcessInterface {

    /**
     * To initiate the process to parse the main file using the GpehFileHandler
     * and determine a list of Sub Files from the Main File.
     * 
     * @param SingleFileTransferInfo
     * @throws RemoteException
     */
    List<SingleFileTransferInfo> postProcess(FTPSessionOperations session,
            SingleFileTransferInfo singleFileInfo);

}
