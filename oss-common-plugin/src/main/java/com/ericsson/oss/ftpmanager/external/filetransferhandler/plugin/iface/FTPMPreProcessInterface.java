package com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface;

import java.util.List;

import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.ftpsessionops.FTPSessionOperations;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;

public interface FTPMPreProcessInterface {

    /**
     * To do some pre-processing. Eg : Build list of sub-files from GPEH main
     * file.
     * 
     * @param SingleFileTransferInfo
     * @throws Exception
     * @throws RemoteException
     */
    List<SingleFileTransferInfo> preProcess(FTPSessionOperations ftpSession,
            SingleFileTransferInfo details);

}
