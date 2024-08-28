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

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.Message;
import org.junit.Test;

public class CFTSplitterTest {

    @Test
    public void testSendCollectionRequests() {
        List<Object> celltracePostProcessList = new LinkedList<>();
        celltracePostProcessList
                .add(0,
                        "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL1_1.bin.gz");
        celltracePostProcessList
                .add(0,
                        "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL1_3.bin.gz");

        CFTSplitter splitter = new CFTSplitter();
        List<Message> singleJobs = splitter
                .sendCollectionRequests(celltracePostProcessList);

        for (Message singleJob : singleJobs) {
            assertTrue(celltracePostProcessList.contains(singleJob.getBody()));
        }
    }
}
