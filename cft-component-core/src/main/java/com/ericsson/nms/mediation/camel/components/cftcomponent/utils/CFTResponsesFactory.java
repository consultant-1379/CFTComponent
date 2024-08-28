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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.ericsson.oss.mediation.core.events.OperationType;
import com.ericsson.oss.mediation.pm.model.*;

/**
 * This class is used to create new instance of FileCollectionJob from
 * SingleFileTransferInfo object contents
 * 
 * @author esersla
 * 
 */
public abstract class CFTResponsesFactory {

    private static final transient Logger LOG = LoggerFactory
            .getLogger(CFTResponsesFactory.class);

    private static Map<String, String> conversion = new HashMap<>();
    private static final String PMACCESS = "ManagedElement=1,PmAccess=1";

    static {
        // If any of network model element names are changed, map values need to
        // be updated
        conversion.put("SubNetwork", "NW");
        conversion.put("NonRootSubNetwork", "SN");
        conversion.put("MeContext", "MeC");
        // conversion.put("ManagedElement", "ManagedElement");
    }

    /**
     * create as equivalent instance of <code>SingleFileTransferInfo</code> for
     * <code>FileCollectionJob</code>
     * 
     * @return
     */
    public static FileCollectionJob createFCJob(
            final SingleFileTransferInfo singleFileTransferInfo) {

        LOG.debug("creating FileCollectionJob for : {}", singleFileTransferInfo);

        final FileCollectionJob fileCollectionJob = new FileCollectionJob();
        fileCollectionJob.setJobId(singleFileTransferInfo.transfer_id);
        fileCollectionJob
                .setDestinationFileName(singleFileTransferInfo.oss_file_name);
        fileCollectionJob
                .setDestinationDirectory(singleFileTransferInfo.oss_path);
        fileCollectionJob
                .setSourceFileName(singleFileTransferInfo.node_file_name);
        fileCollectionJob.setSourceDirectory(singleFileTransferInfo.node_path);
        fileCollectionJob
                .setDecompressionRequired(singleFileTransferInfo.uncompressed_needed);
        String nodeFDNUpdated = singleFileTransferInfo.node_fdn;
        final String rootmo = singleFileTransferInfo.node_fdn.substring(0,
                singleFileTransferInfo.node_fdn.indexOf(","));
        if (rootmo.contains("_R")) {
            nodeFDNUpdated = singleFileTransferInfo.node_fdn.replaceFirst(
                    "_R,", ",");
        }
        final String torFdn = convertOSSFdnInTor(nodeFDNUpdated);
        fileCollectionJob.setNodeAddress(torFdn);
        fileCollectionJob.setProtocolInfo(OperationType.PM.toString());
        fileCollectionJob.setIsCreatedWithPlugin(true);
        return fileCollectionJob;
    }

    /**
     * FDN converted from OSS to Tor
     * 
     * @param nodeFdn
     * @return String
     */
    private static String convertOSSFdnInTor(final String ossFdn) {
        int i = 0;
        String result = "";
        final StringTokenizer ossToken = new StringTokenizer(ossFdn, ",");
        while (ossToken.hasMoreElements()) {
            final String[] fdnPart = ((String) ossToken.nextElement())
                    .split("=");
            // second element could or not to exist
            if (i == 1) {
                result += resolveSecondElement(result, fdnPart);
            } else {
                result += conversion.get(fdnPart[0].trim()) + "=" + fdnPart[1]
                        + ",";
            }
            i++;
        }
        return result + PMACCESS;
    }

    /**
     * @return
     */
    private static String resolveSecondElement(final String result,
            final String[] fdnPart) {
        String resolvedString = "";
        // if it exist we will rename it to Resource Group
        if ("SubNetwork".equalsIgnoreCase(fdnPart[0].trim())) {
            resolvedString = conversion.get("NonRootSubNetwork") + "="
                    + fdnPart[1] + ",";
        } else {
            // if it doesn't exist Resource group will be created and MeContext
            // will be assigned to that group
            resolvedString = conversion.get("NonRootSubNetwork")
                    + result.substring(result.indexOf('='))
                    + conversion.get(fdnPart[0].trim()) + "=" + fdnPart[1]
                    + ",";
        }
        return resolvedString;
    }

    /**
     * @param fileToBeCollected
     */
    public static FileCollectionResult sendFileCollectionResponse(
            final SingleFileTransferInfo fileToBeCollected) {

        LOG.debug("creating FileCollectionResult for : main job {}",
                fileToBeCollected);

        return new FileCollectionSuccess(fileToBeCollected.transfer_id,
                fileToBeCollected.node_file_name, fileToBeCollected.node_path,
                fileToBeCollected.oss_file_name, fileToBeCollected.oss_path,
                fileToBeCollected.startTime, new Date().getTime(),
                fileToBeCollected.bytesStored, fileToBeCollected.bytesStored);
    }

}
