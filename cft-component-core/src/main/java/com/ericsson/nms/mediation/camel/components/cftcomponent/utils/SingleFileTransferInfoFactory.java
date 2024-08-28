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
package com.ericsson.nms.mediation.camel.components.cftcomponent.utils;

import static com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTConstants.*;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.NodeInfo;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;

/**
 * This class is used for creating new instance of SingleFileTransferInfo from
 * the FileCollectionJob instance received by CFTComponent
 * 
 * @author esersla
 * 
 */
public abstract class SingleFileTransferInfoFactory {

    private static final transient Logger LOG = LoggerFactory
            .getLogger(SingleFileTransferInfoFactory.class);

    /**
     * create as equivalent instance of <code>SingleFileTransferInfo</code> for
     * <code>FileCollectionJob</code>
     * 
     * @param exchange
     *            camel exchange with Header containing all the parameters for
     *            creating SingleFileTransferInfo
     * 
     * @return an instance of SingleFileTransferInfo
     */
    public static SingleFileTransferInfo createSFTInfo(final Exchange exchange) {
        LOG.debug(
                " converting CompositeFileCollectionJob {} to SingleFileTransferInfo",
                exchange);
        final SingleFileTransferInfo singleFileTransferInfo = new SingleFileTransferInfo();

        singleFileTransferInfo.transfer_id = (String) exchange.getIn()
                .getHeader(FC_JOB_JOB_ID);
        singleFileTransferInfo.oss_file_name = (String) exchange.getIn()
                .getHeader(CFT_DESTINATION_FILE);
        singleFileTransferInfo.oss_path = (String) exchange.getIn().getHeader(
                CFT_DESTINATION_DIRECTORY);
        singleFileTransferInfo.node_file_name = (String) exchange.getIn()
                .getHeader(CFT_SOURCE_FILE);
        singleFileTransferInfo.node_path = (String) exchange.getIn().getHeader(
                CFT_SOURCE_DIRECTORY);

        singleFileTransferInfo.plugin_class_name = (String) exchange.getIn()
                .getHeader(PLUGIN_NAME);

        singleFileTransferInfo.uncompressed_needed = Boolean
                .valueOf((String) exchange.getIn().getHeader(
                        DECOMPRESSION_REQUIRED));
        singleFileTransferInfo.node_fdn = singleFileTransferInfo.transfer_id
                .split("::")[0];
        singleFileTransferInfo.ropEndTime = Long
                .parseLong(singleFileTransferInfo.transfer_id.split("::")[3]);

        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.node_fdn = singleFileTransferInfo.transfer_id.split("::")[0];
        nodeInfo.timeZone = (String) exchange.getIn().getHeader(TIME_ZONE);

        singleFileTransferInfo.nodeInfo = nodeInfo;

        singleFileTransferInfo.nodeInfo = nodeInfo;

        return singleFileTransferInfo;
    }
}
