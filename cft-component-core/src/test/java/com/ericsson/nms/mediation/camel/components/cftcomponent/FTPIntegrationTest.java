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
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.jndi.JndiContext;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.*;

import com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTConstants;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.CFTSplitter;
import com.ericsson.oss.mediation.camel.components.eftp.pool.Constants;
import com.ericsson.oss.mediation.camel.components.eftp.pool.FtpConnectionPool;
import com.ericsson.oss.mediation.pm.model.CompositeFileCollectionJob;

public class FTPIntegrationTest extends CamelTestSupport {

    private static final String SOURCE_DIR = "/pm_data/ftp/";
    private static final String NODE_ADD = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001";
    private static final String COMPOSITE_TRANSFER_ROUTE_ID = "route1";
    private static final String USERNAME = "test";
    private static final int PORT = 58533;
    private static FtpServer server;

    private static final int poolSize = 600;

    private static final int maxActive = 4;

    private static final int maxIdle = 4;

    private static final long waitTime = 2000;

    private static final long evictionEligibleAfter = 5000;

    private static final long evictionThreadRunTime = 30000;

    private static FtpConnectionPool ftpPool;

    @BeforeClass
    public static void startFtpServer() throws Exception {

        System.setProperty("pmsdatadir",
                "var/opt/ericsson/nms_umts_pms_seg/data/");

        System.setProperty("uetraceCfgFile",
                "src/test/resources/UeTraceInfo.txt");

        final FtpServerFactory serverFactory = new FtpServerFactory();
        final ListenerFactory factory = new ListenerFactory();
        factory.setPort(PORT);
        serverFactory.addListener("default", factory.createListener());
        server = serverFactory.createServer();
        final PropertiesUserManagerFactory userFactory = new PropertiesUserManagerFactory();

        /**
         * not used atm, using defaults
         */
        // final File userFile = new
        // File("src\\test\\resources\\user.properties");
final File userHome = new File("src\\test\\resources");
        userHome.setWritable(true);
        userHome.mkdirs();
        // userFactory.setFile(userFile);
        final UserManager um = userFactory.createUserManager();
        final BaseUser user = new BaseUser();
        user.setName(USERNAME);
        user.setPassword(USERNAME);
        user.setHomeDirectory(userHome.getAbsolutePath());

        final List<Authority> auths = new ArrayList<Authority>();
        final Authority auth = new WritePermission();
        auths.add(auth);
        user.setAuthorities(auths);

        um.save(user);

        serverFactory.setUserManager(um);
        server.start();

        ftpPool = new FtpConnectionPool(maxActive, maxIdle, poolSize, waitTime,
                evictionEligibleAfter, evictionThreadRunTime, 10);

    }

    @AfterClass
    public static void shutdownFtpServer() {
        if (server != null) {
            server.stop();
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
                        .split()
                        .method(new CFTSplitter(), "sendCollectionRequests")
                        .to("mock:result").setId(COMPOSITE_TRANSFER_ROUTE_ID);
            }
        };
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        JndiContext ctx = new JndiContext();
        ctx.bind(Constants.FTP_POOL, ftpPool);
        return new DefaultCamelContext(ctx);
    }

    @Test
    public void testProcess() throws Exception {
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
        assertMockEndpointsSatisfied();
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
        exchange.getIn().setHeader(CFTConstants.CFT_SOURCE_DIRECTORY,
                "/pm_data/");

        final Producer producer = start.createProducer();
        producer.process(exchange);

        mock.expectedMinimumMessageCount(1);
        assertMockEndpointsSatisfied();
    }

    CompositeFileCollectionJob createCFCJob(final String pluginName,
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

    private Exchange getExchange(final Endpoint ep, final String pluginName,
            final String destDir, final String destFile, final String jobId,
            final String srcFile) throws Exception {
        final Exchange ex = ep.createExchange();
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_IP_ADDRESS, "localhost");
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_PASSWORD, USERNAME);
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_USERNAME, USERNAME);
        ex.getIn().setHeader(CFTConstants.CFT_TARGET_PORT, PORT);
        ex.getIn().setHeader("secureFtp", "false");
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
}
