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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.*;
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.ftpsessionops.FTPSessionOperations;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;

@RunWith(MockitoJUnitRunner.class)
public class CFTProcessorTest {

    private static final String FDN = "TestFDN";
    @Mock
    private FTPSessionOperations sessionOperations;

    @Mock
    private FTPMPluginFactoryInterface pluginForJob;

    private SingleFileTransferInfo fileInfo;

    private CFTProcessor processor;

    @Before
    public void setUp() {
        processor = new CFTProcessor();

        fileInfo = new SingleFileTransferInfo();
        fileInfo.node_fdn = FDN;
        when(pluginForJob.getPreProcessor()).thenReturn(new PreProcessor());
        when(pluginForJob.getPostProcessor()).thenReturn(new PreProcessor());

    }

    @Test
    public void testPreProcessingCollectionJob() throws CompositeFTPException {
        final List<SingleFileTransferInfo> filesToBeCollected = new ArrayList<>();
        assertEquals(
                1,
                processor.preProcessCollectionJob(fileInfo, pluginForJob,
                        filesToBeCollected, sessionOperations).size());
        assertEquals(
                FDN,
                processor.preProcessCollectionJob(fileInfo, pluginForJob,
                        filesToBeCollected, sessionOperations).get(0).node_fdn);

    }

    @Test
    public void testPostProcessingCollectionJob() throws CompositeFTPException {
        final List<SingleFileTransferInfo> filesToBeCollected = new ArrayList<>();
        filesToBeCollected.add(new SingleFileTransferInfo());

        assertEquals(
                2,
                processor.postProcessCollectionJob(fileInfo, pluginForJob,
                        filesToBeCollected, sessionOperations).size());
        assertEquals(
                FDN,
                processor.postProcessCollectionJob(fileInfo, pluginForJob,
                        filesToBeCollected, sessionOperations).get(0).node_fdn);

    }

    private class PreProcessor implements FTPMPreProcessInterface,
            FTPMPostProcessInterface {

        @Override
        public List<SingleFileTransferInfo> preProcess(
                final FTPSessionOperations ftpSession,
                final SingleFileTransferInfo details) {
            final List<SingleFileTransferInfo> list = new ArrayList<>();
            list.add(new SingleFileTransferInfo());
            return list;
        }

        @Override
        public List<SingleFileTransferInfo> postProcess(
                final FTPSessionOperations session,
                final SingleFileTransferInfo singleFileInfo) {
            final List<SingleFileTransferInfo> list = new ArrayList<>();
            list.add(new SingleFileTransferInfo());
            return list;
        }

    }
}
