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
package com.ericsson.nms.mediation.camel.components.cftcomponent.processor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.CFTResponsesFactory;
import com.ericsson.oss.ftpmanager.common.filetransfer.exceptions.FTPException; //NOPMD
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.*;
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.ftpsessionops.FTPSessionOperations;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.ericsson.oss.mediation.pm.model.FileCollectionResult;

/**
 * Responsible for pre-processing or post-processing of the Composite file
 * collection job Operation to be performed is chosen by the plugin class thats
 * being loaded from the shared storage
 * 
 * 
 * @author esersla
 * 
 */
public class CFTProcessor {

    private static final transient Logger LOG = LoggerFactory
            .getLogger(CFTProcessor.class);

    /**
     * This method will check whether the plug-in is instance of
     * <code>FTPMPreProcessInterface</code> If yes, <code>preProcess()</code>
     * method will give the list of files that needs to be collected
     * 
     * @param session
     *            FTPSessionOperations interface implementation
     * @param fileInfo
     *            the main job being received in first place
     * @param node_fdn
     *            OSS representation of NE FDN
     * @param pluginForJob
     *            the instance of FTPM plug-in created in CFTComponent
     * @param filesToBeCollected
     *            list of files returned which needs to collected contains main
     *            job also
     * @return filesToBeCollected list of files returned which needs to
     *         collected
     * @throws CompositeFTPException
     */
    public List<SingleFileTransferInfo> preProcessCollectionJob(
            final SingleFileTransferInfo fileInfo,
            final FTPMPluginFactoryInterface pluginForJob,
            List<SingleFileTransferInfo> filesToBeCollected,
            final FTPSessionOperations sessionOperations)
            throws CompositeFTPException {

        LOG.debug(" preProcessCollectionJob() called");
        final FTPMPreProcessInterface preProcessor = pluginForJob
                .getPreProcessor();

        if (preProcessor != null) {

            LOG.debug("Calling pre-processing on {}", preProcessor.getClass());

            fileInfo.interceptionStartTime = System.currentTimeMillis();

            filesToBeCollected = preProcessor.preProcess(sessionOperations,
                    fileInfo);

            setNodeFdnToSingleFileTransferInfo(fileInfo.node_fdn,
                    filesToBeCollected);

            fileInfo.interceptionEndtime = System.currentTimeMillis();

            LOG.debug("Number of files created for pre processing are : {}",
                    filesToBeCollected.size());

        } else {
            LOG.debug(
                    "No pre-processor available for file collection job : {}",
                    fileInfo.transfer_id);
        }
        return filesToBeCollected;
    }

    /**
     * This method checks for the <code>FTPMPostProcessInterface</code>
     * implementation in plug-in class. It uses file which is already collected
     * as part of <code>FileCollectionJob</code> received by the
     * <b>CFTComponent</b> and the FTPSessionOperation to get list of remaining
     * sub-files that needs to be collected from the NE.
     * 
     * @param fileInfo
     *            instance of file collection job received by component, already
     *            collected
     * @param node_fdn
     *            String representing the Network Element FDN
     * @param pluginForJob
     *            the FTPM plug-in class instance
     * @param sessionOperations
     * @return filesToBeCollected list of files that needs to be collected
     *         provided by <code>postProcess()</code> method of
     *         <code>FTPMPostProcessInterface</code>
     * @throws CompositeFTPException
     */
    public List<SingleFileTransferInfo> postProcessCollectionJob(
            final SingleFileTransferInfo fileInfo,
            final FTPMPluginFactoryInterface pluginForJob,
            final List<SingleFileTransferInfo> filesToBeCollected,
            final FTPSessionOperations sessionOperations)
            throws CompositeFTPException {

        final FTPMPostProcessInterface postProcessor = pluginForJob
                .getPostProcessor();

        final int noOfExistingFiles = filesToBeCollected.size();

        if (postProcessor != null) {

            LOG.debug("Calling post-processing on {}", postProcessor.getClass());

            fileInfo.interceptionStartTime = System.currentTimeMillis();

            filesToBeCollected.addAll(postProcessor.postProcess(
                    sessionOperations, fileInfo));

            setNodeFdnToSingleFileTransferInfo(fileInfo.node_fdn,
                    filesToBeCollected);

            fileInfo.interceptionEndtime = System.currentTimeMillis();

            LOG.debug("Number of files created for post processing are : {}",
                    (filesToBeCollected.size() - noOfExistingFiles));

        } else {
            LOG.debug(
                    "No post-processor available for file collection job : {}",
                    fileInfo.transfer_id);
        }
        return filesToBeCollected;
    }

    /**
     * It sets the node FDN in the file collection jobs created by the plug-in
     * 
     * @param node_fdn
     * @param filesToBeCollected
     */
    private void setNodeFdnToSingleFileTransferInfo(final String node_fdn,
            final List<SingleFileTransferInfo> filesToBeCollected) {
        LOG.debug(" setting the node FDN {} for plugin-processed files {}",
                node_fdn);
        for (final SingleFileTransferInfo singleFiletransferinfo : filesToBeCollected) {
            singleFiletransferinfo.node_fdn = node_fdn;
        }
    }

    /**
     * It will perform the <code>FTPSessionOperation's</code>
     * <code>collectFile()</code> method & store the file on the OSS file
     * storage
     * 
     * @param filesToBeCollected
     *            this list of files to be collected
     * @param sessionOperations
     * @throws CompositeFTPException
     * @throws FTPException
     */
    public FileCollectionResult collectFile(
            final List<SingleFileTransferInfo> filesToBeCollected,
            final FTPSessionOperations sessionOperations)
            throws CompositeFTPException {
        LOG.debug(" collectFile() called");
        SingleFileTransferInfo fileToBeCollected = null;
        boolean isSuccessful = false;
        if (filesToBeCollected.size() == 1) {
            fileToBeCollected = filesToBeCollected.get(0);
            if (needToCollect(fileToBeCollected)) {
            	try {
            		fileToBeCollected = filesToBeCollected.remove(0);
                    isSuccessful = sessionOperations
                            .collectFile(fileToBeCollected);
                    fileToBeCollected.transferSucceeded = isSuccessful;
                    return CFTResponsesFactory
                            .sendFileCollectionResponse(fileToBeCollected);
                } catch (Exception e) {
                    LOG.error(
                            "File Transfer Exception {} occurred during collection operation of the file {}",
                            e, fileToBeCollected);
                    throw new CompositeFTPException(
                            "File Transfer Exception {} occurred during collection operation of the file ");
                }
            }
        } else {
            LOG.debug(" Received list of sub-files after process of main composite job, "
                    + "now sending the list of files Mes Component to redistribute these FileCollectionJob");
        }
        return null;
    }

    /**
     * @param fileToBeCollected
     * @return
     */
    private boolean needToCollect(final SingleFileTransferInfo fileToBeCollected) {
        return fileToBeCollected.node_file_name
                .contains("CellTraceFilesLocation")
                || fileToBeCollected.node_file_name
                        .contains("UeTraceFilesLocation");
    }

}
