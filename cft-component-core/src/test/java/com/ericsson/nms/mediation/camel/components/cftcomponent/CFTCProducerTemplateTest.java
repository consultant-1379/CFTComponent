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
package com.ericsson.nms.mediation.camel.components.cftcomponent;

import java.io.File;
import java.util.*;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.jndi.JndiContext;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.*;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.filesystem.NativeFileSystemFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTConstants;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.CFTResponsesFactory;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.SingleFileTransferInfoFactory;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.ericsson.oss.mediation.camel.components.eftp.pool.SftpConnectionPool;
import com.ericsson.oss.mediation.pm.model.*;

public class CFTCProducerTemplateTest extends CamelTestSupport {

    private static final String COMPOSITE_TRANSFER_ROUTE_ID = "route1";
    private static final String SOURCE_DIR = "src/test/resources/pm_data/";
    private static final String NODE_ADD = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001";
    private static SshServer sshd;
    private static final String USERNAME = "test";
    private static final String PASSWD = "secret";
    private static final int PORT = 58558;

    private static final int poolSize = 500;

    private static final int maxActive = 1;

    private static final int maxIdle = 1;

    private static final long waitTime = 2000;

    private static final long evictionEligibleAfter = 10000;

    private static final long evictionThreadRunTime = 5000;

    private static SftpConnectionPool sftpPool;

    private static final String className = "com.ericsson.nms.umts.ranos.pms.collectionplugins.gpeh.GpehJobPlugin";
    private static Map<Integer, ExpectedMessage> celltracePostProcessList;
    private static Map<Integer, ExpectedMessage> uetracePostProcessList;

    @BeforeClass
    public static void startSSHService() {
        System.setProperty("pmsdatadir",
                "var/opt/ericsson/nms_umts_pms_seg/data/");
        prepareDestFileList();
        cleanDestDir();
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(PORT);
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(final String username,
                    final String password, final ServerSession session) {
                if (username.equals(USERNAME) && password.equals(PASSWD)) {
                    return true;
                }
                return false;
            }
        });
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setFileSystemFactory(new NativeFileSystemFactory());
        sshd.setCommandFactory(new ScpCommandFactory());

        final List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>(
                1);
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);

        final List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();

        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);
        try {
            sshd.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        System.setProperty("uetraceCfgFile",
                "src/test/resources/UeTraceInfo.txt");

        sftpPool = new SftpConnectionPool(maxActive, maxIdle, poolSize,
                waitTime, evictionEligibleAfter, evictionThreadRunTime, 250);

    }

    private static void cleanDestDir() {
        final File dest = new File("src/test/resources/segment1/");
        //        final File dataDir = new File(
        //                "\\var\\opt\\ericsson\\nms_umts_pms_seg\\data\\");
        //        dataDir.mkdirs();
        if (dest.exists() && dest.canWrite()) {
            for (File oldFile : dest.listFiles()) {
                oldFile.delete();
            }
        }
    }

    public class SomeBeanSplitter {
        public List<Message> splitMessage(@Body List<Object> messageList) {
            List<Message> answer = new ArrayList<Message>();
            for (Object singleMessage : messageList) {
                DefaultMessage message = new DefaultMessage();
                message.setBody(singleMessage);
                answer.add(message);
            }
            return answer;
        }

    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                context.setTracing(true);

                from("direct:startCompositeTest")
                        .autoStartup(true)
                        .to("cftc://ericsson123?ftPluginJarDirectory=src/test/resources")
                        .split().method(new SomeBeanSplitter(), "splitMessage")

                        .to("mock:result").setId(COMPOSITE_TRANSFER_ROUTE_ID);
            }
        };
    }

    private Exchange getExchange(final Endpoint ep, final String pluginName,
            final String destDir, final String destFile, final String jobId,
            final String srcFile) throws Exception {
        final Exchange ex = ep.createExchange();
        ex.getIn().setHeader(CFTConstants.CLASS_NAME, className);
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_IP_ADDRESS, "localhost");
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_PASSWORD, PASSWD);
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_USERNAME, USERNAME);
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_PORT, PORT);
        ex.getIn().setHeader("secureFtp", "true");
        ex.getIn().setHeader("fake name", "fake name");
        ex.getIn().setBody("CompositeTest");
        ex.getIn().setHeader(CFTConstants.DECOMPRESSION_REQUIRED, "true");
        ex.getIn().setHeader(CFTConstants.CFT_DESTINATION_DIRECTORY, destDir);
        ex.getIn().setHeader(CFTConstants.CFT_DESTINATION_FILE, destFile);
        ex.getIn().setHeader(CFTConstants.FC_JOB_JOB_ID, jobId);
        ex.getIn().setHeader(CFTConstants.CFT_SOURCE_DIRECTORY, SOURCE_DIR);
        ex.getIn().setHeader(CFTConstants.CFT_SOURCE_FILE, srcFile);
        ex.getIn().setHeader(CFTConstants.PLUGIN_NAME, pluginName);
        ex.getIn().setHeader(CFTConstants.TIME_ZONE, "GMT");
        return ex;
    }

    @Test
    public void testProcessForPostProcessCellTraceCollectionJob()
            throws Exception {

        final MockEndpoint mock = getMockEndpoint("mock:result");
        final Route route = this.context.getRoutes().get(0);
        final Endpoint start = route.getEndpoint();
        final Exchange exchange = getExchange(
                start,
                "com.ericsson.nms.umts.ranos.pms.collectionplugins.celltrace.CellTraceJobPlugin",
                "src/test/resources/segment1/CELLTRACE",
                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_CellTraceFilesLocation",
                "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::CELL_TRACE::2::1361961900000",
                "CellTraceFilesLocation");
        final Producer producer = start.createProducer();
        producer.process(exchange);

        mock.expectedMinimumMessageCount(6);
        // for (Exchange resultList : mock.getExchanges()) {
        // for (FileCollectionJob result :
        // ((List<FileCollectionJob>)resultList.getIn().getBody())){
        // assertTrue(celltracePostProcessList.contains(result.getDestinationFileName()));
        // }
        // }
        assertMockEndpointsSatisfied();

    }

    @Test
    public void testProcessForPreProcessDeltaSyncCollectionJob()
            throws Exception {

        final Route route = this.context.getRoutes().get(0);
        if (route == null) {
            fail("Route is null");
        }
        final Endpoint start = route.getEndpoint();
        final Exchange exchange = getExchange(
                start,
                "com.ericsson.nms.umts.ranos.pms.collectionplugins.deltasync.DeltaSyncJobPlugin",
                "src/test/resources/segment1/XML",
                "",
                "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::DELTA_SYNC::1::1361973600000",
                "");
        final Producer producer = start.createProducer();
        producer.process(exchange);
    }

    @Test
    public void testProcessForPostProcessUeTraceCollectionJob()
            throws Exception {

        final MockEndpoint mock = getMockEndpoint("mock:result");

        final Route route = this.context.getRoutes().get(0);
        final Endpoint start = route.getEndpoint();
        final Exchange exchange = getExchange(
                start,
                "com.ericsson.nms.umts.ranos.pms.collectionplugins.uetrace.UeTraceJobPlugin",
                "src/test/resources/segment1/UETRACE_POST",
                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_UeTraceFilesLocation",
                "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
                "UeTraceFilesLocation");
        final Producer producer = start.createProducer();
        producer.process(exchange);

        mock.expectedMinimumMessageCount(7);
        // for (Exchange resultList : mock.getExchanges()) {
        // for (FileCollectionJob result :
        // (List<FileCollectionJob>)resultList.getIn().getBody()){
        // assertTrue(uetracePostProcessList.contains(result.getDestinationFileName()));
        // }
        // }
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testProcessForPreProcessUeTraceCollectionJob() throws Exception {

        final MockEndpoint mock = getMockEndpoint("mock:result");

        final Route route = this.context.getRoutes().get(0);
        final Endpoint start = route.getEndpoint();
        final Exchange exchange = getExchange(
                start,
                "com.ericsson.nms.umts.ranos.pms.collectionplugins.uetrace.UeTraceJobPlugin",
                "src/test/resources/segment1/UETRACE_PRE",
                "",
                "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
                "");
        final Producer producer = start.createProducer();
        producer.process(exchange);

        mock.expectedMinimumMessageCount(1);
        // for (Exchange resultList : mock.getExchanges()) {
        // for (FileCollectionJob result :
        // ((List<FileCollectionJob>)resultList.getIn().getBody())){
        // assertTrue(uetracePreProcessList.contains(result.getDestinationFileName()));
        // }
        // }
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testTimeZoneInDestinationFileName() throws Exception {
        System.setProperty("uetraceCfgFile",
                "src/test/resources/UeTraceInfo.txt");
        final MockEndpoint mock = getMockEndpoint("mock:result");

        final Route route = this.context.getRoutes().get(0);
        final Endpoint start = route.getEndpoint();
        final Exchange exchange = getExchange(
                start,
                "com.ericsson.nms.umts.ranos.pms.collectionplugins.uetrace.UeTraceJobPlugin",
                "src/test/resources/segment1/UETRACE_POST",
                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_UeTraceFilesLocation",
                "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
                "UeTraceFilesLocation");
        final Producer producer = start.createProducer();
        exchange.getIn().setHeader(CFTConstants.TIME_ZONE, "EST5EDT");
        List<Message> expectedbodies = createExpectedMessageList(exchange);
        producer.process(exchange);
        List<Exchange> receivedbodies = mock.getReceivedExchanges();

        mock.expectedMessageCount(7);
        assertTrue(compareReceivedWithExpected(receivedbodies, expectedbodies));

        assertMockEndpointsSatisfied();
    }

    /**
     * @param expectedbodies
     * @param receivedbodies
     * @return
     * 
     */
    private boolean compareReceivedWithExpected(List<Exchange> receivedbodies,
            List<Message> expectedbodies) {
        boolean expectationMatches = false;
        for (Exchange receivedBody : receivedbodies) {
            expectationMatches = false;
            for (Message expectedBody : expectedbodies) {
                if (compareFileCollectionJobs(receivedBody, expectedBody)) {
                    expectationMatches = true;
                    break;
                }
            }
            if (!expectationMatches) {
                break;
            }
        }
        return expectationMatches;
    }

    /**
     * @param receivedBody
     * @param expectedBody
     * @return
     */
    private boolean compareFileCollectionJobs(Exchange receivedBody,
            Message expectedBody) {
        final Object receivedFCJ = receivedBody.getIn().getBody();
        final Object expectedFCJ = expectedBody.getBody();
        if (!receivedFCJ.getClass().equals(expectedFCJ.getClass())) {
            return false;
        }
        if (receivedFCJ.getClass().toString().contains("FileCollectionJob")) {
            if (!((FileCollectionJob) receivedFCJ).getNodeAddress().equals(
                    ((FileCollectionJob) expectedFCJ).getNodeAddress())) {
                return false;
            }
            if (!((FileCollectionJob) receivedFCJ).getProtocolInfo().equals(
                    ((FileCollectionJob) expectedFCJ).getProtocolInfo())) {
                return false;
            }
            if (!((FileCollectionJob) receivedFCJ).getIsCreatedWithPlugin()
                    .equals(((FileCollectionJob) expectedFCJ)
                            .getIsCreatedWithPlugin())) {
                return false;
            }
            if (!((FileCollectionJob) receivedFCJ).getDestinationDirectory()
                    .equals(((FileCollectionJob) expectedFCJ)
                            .getDestinationDirectory())) {
                return false;
            }
            if (!((FileCollectionJob) receivedFCJ).getDestinationFileName()
                    .equals(((FileCollectionJob) expectedFCJ)
                            .getDestinationFileName())) {
                return false;
            }
            if (!((FileCollectionJob) receivedFCJ).getSourceDirectory().equals(
                    ((FileCollectionJob) expectedFCJ).getSourceDirectory())) {
                return false;
            }
            if (!((FileCollectionJob) receivedFCJ).getSourceFileName().equals(
                    ((FileCollectionJob) expectedFCJ).getSourceFileName())) {
                return false;
            }
        } else if (receivedFCJ.getClass().toString()
                .contains("FileCollectionSuccess")) {
            if (!((FileCollectionSuccess) receivedFCJ)
                    .getDestinationDirectory().equals(
                            ((FileCollectionSuccess) expectedFCJ)
                                    .getDestinationDirectory())) {
                return false;
            }
            if (!((FileCollectionSuccess) receivedFCJ).getDestinationFileName()
                    .equals(((FileCollectionSuccess) expectedFCJ)
                            .getDestinationFileName())) {
                return false;
            }
            if (!((FileCollectionSuccess) receivedFCJ).getDestinationFileName()
                    .equals(((FileCollectionSuccess) expectedFCJ)
                            .getDestinationFileName())) {
                return false;
            }
            if (!((FileCollectionSuccess) receivedFCJ).getSourceDirectory()
                    .equals(((FileCollectionSuccess) expectedFCJ)
                            .getSourceDirectory())) {
                return false;
            }
        }
        return true;
    }

    public CompositeFileCollectionJob createCFCJob(final String pluginName,
            final String destDir, final String destFile, final String jobId,
            final String srcFile) {

        final CompositeFileCollectionJob compositeJob = new CompositeFileCollectionJob();
        compositeJob.setDecompressionRequired(true);
        compositeJob.setDestinationDirectory(destDir);
        compositeJob.setDestinationFileName(destFile);
        compositeJob.setJobId(jobId);
        compositeJob.setNodeAddress(NODE_ADD);
        compositeJob.setSourceDirectory(SOURCE_DIR);
        compositeJob.setSourceFileName(srcFile);
        compositeJob.setPluginName(pluginName);
        return compositeJob;
    }

    List<Message> createExpectedMessageList(Exchange exchange) {
        List<Message> msgs = new ArrayList<Message>();

        final SingleFileTransferInfo firstjob = SingleFileTransferInfoFactory
                .createSFTInfo(exchange);

        for (ExpectedMessage exmsg : uetracePostProcessList.values()) {
            final FileCollectionJob compositeJob = new FileCollectionJob();
            compositeJob.setDecompressionRequired(false);
            compositeJob.setDestinationDirectory(exmsg.desd);
            compositeJob.setIsCreatedWithPlugin(true);
            compositeJob.setDestinationFileName(exmsg.desf);
            compositeJob
                    .setJobId("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::5::1361961900000");
            compositeJob
                    .setNodeAddress("NW=ONRM_ROOT_MO,SN=ERBS-SUBNW-1,MeC=ERBSM_12B00001,ManagedElement=1,PmAccess=1");
            compositeJob.setSourceDirectory(exmsg.srd);
            compositeJob.setSourceFileName(exmsg.srf);
            Message msg = new DefaultMessage();
            msg.setBody(compositeJob);
            msgs.add(msg);
        }
        Message msg = new DefaultMessage();
        firstjob.oss_path = "var/opt/ericsson/nms_umts_pms_seg/data/";
        msg.setBody(CFTResponsesFactory.sendFileCollectionResponse(firstjob));
        msgs.add(msg);
        return msgs;
    }

    static void prepareDestFileList() {
        celltracePostProcessList = new HashMap<Integer, ExpectedMessage>();
        celltracePostProcessList
                .put(0,
                        new ExpectedMessage(
                                "",
                                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL1_1.bin.gz",
                                "", ""));
        celltracePostProcessList
                .put(1,
                        new ExpectedMessage(
                                "",
                                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL1_3.bin.gz",
                                "", ""));
        celltracePostProcessList
                .put(2,
                        new ExpectedMessage(
                                "",
                                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL2_1.bin.gz",
                                "", ""));
        celltracePostProcessList
                .put(3,
                        new ExpectedMessage(
                                "",
                                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL2_3.bin.gz",
                                "", ""));
        celltracePostProcessList
                .put(4,
                        new ExpectedMessage(
                                "",
                                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL3_1.bin.gz",
                                "", ""));
        celltracePostProcessList
                .put(5,
                        new ExpectedMessage(
                                "",
                                "A20130227.1030-1045_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_celltracefile_DUL3_3.bin.gz",
                                "", ""));

        uetracePostProcessList = new HashMap<Integer, ExpectedMessage>();
        uetracePostProcessList
                .put(0,
                        new ExpectedMessage(
                                "A20130227.1030-1045_uetrace_353057010010.bin.gz",
                                "A20130227.0530-0500-0545-0500_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_353057010010_001001010006_uetracefile.bin.gz",
                                "src/test/resources/pm_data3/",
                                "src/test/resources/segment1/" + "UETRACE_POST"
                                        + File.separator + "353057010010"
                                        + File.separator
                                        + "SubNetwork=ERBS-SUBNW-1"
                                        + File.separator
                                        + "MeContext=ERBSM_12B00001"));
        uetracePostProcessList
                .put(1,
                        new ExpectedMessage(
                                "A20130227.1030-1045_uetrace_45604501000A.bin.gz",
                                "A20130227.0530-0500-0545-0500_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_45604501000A_001001010005_uetracefile.bin.gz",
                                "src/test/resources/pm_data3/",
                                "src/test/resources/segment1/" + "UETRACE_POST"
                                        + File.separator + "45604501000A"
                                        + File.separator
                                        + "SubNetwork=ERBS-SUBNW-1"
                                        + File.separator
                                        + "MeContext=ERBSM_12B00001"));
        uetracePostProcessList
                .put(2,
                        new ExpectedMessage(
                                "A20130227.1030-1045_uetrace_45604501000B.bin.gz",
                                "A20130227.0530-0500-0545-0500_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_45604501000B_001001010004_uetracefile.bin.gz",
                                "src/test/resources/pm_data2/",
                                "src/test/resources/segment1/" + "UETRACE_POST"
                                        + File.separator + "45604501000B"
                                        + File.separator
                                        + "SubNetwork=ERBS-SUBNW-1"
                                        + File.separator
                                        + "MeContext=ERBSM_12B00001"));
        uetracePostProcessList
                .put(3,
                        new ExpectedMessage(
                                "A20130227.1030-1045_uetrace_45604501000C.bin.gz",
                                "A20130227.0530-0500-0545-0500_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_45604501000C_001001010003_uetracefile.bin.gz",
                                "src/test/resources/pm_data2/",
                                "src/test/resources/segment1/" + "UETRACE_POST"
                                        + File.separator + "45604501000C"
                                        + File.separator
                                        + "SubNetwork=ERBS-SUBNW-1"
                                        + File.separator
                                        + "MeContext=ERBSM_12B00001"));
        uetracePostProcessList
                .put(4,
                        new ExpectedMessage(
                                "A20130227.1030-1045_uetrace_45604501000D.bin.gz",
                                "A20130227.0530-0500-0545-0500_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_45604501000D_001001010002_uetracefile.bin.gz",
                                "src/test/resources/pm_data1/",
                                "src/test/resources/segment1/" + "UETRACE_POST"
                                        + File.separator + "45604501000D"
                                        + File.separator
                                        + "SubNetwork=ERBS-SUBNW-1"
                                        + File.separator
                                        + "MeContext=ERBSM_12B00001"));
        uetracePostProcessList
                .put(5,
                        new ExpectedMessage(
                                "A20130227.1030-1045_uetrace_45604501001a.bin.gz",
                                "A20130227.0530-0500-0545-0500_SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001_45604501001a_unknown_uetracefile.bin.gz",
                                "src/test/resources/pm_data1/",
                                "src/test/resources/segment1/" + "UETRACE_POST"
                                        + File.separator + "45604501001a"
                                        + File.separator
                                        + "SubNetwork=ERBS-SUBNW-1"
                                        + File.separator
                                        + "MeContext=ERBSM_12B00001"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.camel.test.junit4.CamelTestSupport#createCamelContext()
     */
    @Override
    protected CamelContext createCamelContext() throws Exception {
        final JndiContext ctx = new JndiContext();
        ctx.bind("sftpPool", sftpPool);
        return new DefaultCamelContext(ctx);
    }

}

class ExpectedMessage {
    String srf;
    String desf;
    String srd;
    String desd;

    ExpectedMessage(String srF, String desF, String srD, String desD) {
        srf = srF;
        desf = desF;
        srd = srD;
        desd = desD;
    }

    @Override
    public String toString() {
        return srf + " + " + desf + " + " + srd + " + " + desd;
    }
}
