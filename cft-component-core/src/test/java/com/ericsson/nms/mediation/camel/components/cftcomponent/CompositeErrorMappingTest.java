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

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
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
import org.junit.*;

import com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTConstants;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CustomCftException;
import com.ericsson.oss.mediation.camel.components.eftp.pool.SftpConnectionPool;
import com.ericsson.oss.mediation.pm.model.CompositeFileCollectionJob;

public class CompositeErrorMappingTest extends CamelTestSupport {

	private static final String COMPOSITE_TRANSFER_ROUTE_ID = "route1";
	private static final String SOURCE_DIR = "src/test/resources/pm_data/";
	private static SshServer sshd;
	private static final String USERNAME = "test";
	private static final String PASSWD = "secret";
	private static final int PORT = 58558;

	private static final int poolSize = 600;

	private static final int maxActive = 4;

	private static final int maxIdle = 1;

	private static final long waitTime = 2000;

	private static final long evictionEligibleAfter = 5000;

	private static final long evictionThreadRunTime = 30000;

	private static SftpConnectionPool sftpPool;

	@BeforeClass
	public static void startSSHService() {
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
				waitTime, evictionEligibleAfter, evictionThreadRunTime, 10)
				;

	}

	@AfterClass
	public static void shutdownSSHService() {
		try {
			if (sshd != null) {
				sshd.stop(true);
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				context.setTracing(true);

				from("direct:startCompositeTest")
						.errorHandler(deadLetterChannel("mock:error"))
						.to("cftc://ericsson123?ftPluginJarDirectory=src/test/resources")
						.setId(COMPOSITE_TRANSFER_ROUTE_ID);
			}
		};
	}

	private Exchange getExchange(final Endpoint ep, final String pluginName,
			final String destDir, final String destFile, final String jobId,
			final String srcFile) throws Exception {
		final Exchange ex = ep.createExchange();
		ex.getIn().setHeader(CFTConstants.CFT_TARGET_IP_ADDRESS, "localhost");
		ex.getIn().setHeader(CFTConstants.CFT_TARGET_PASSWORD, PASSWD);
		ex.getIn().setHeader(CFTConstants.CFT_TARGET_USERNAME, USERNAME);
		ex.getIn().setHeader(CFTConstants.CFT_TARGET_PORT, PORT);
		ex.getIn().setHeader("secureFtp", "true");
		ex.getIn().setHeader(CFTConstants.DECOMPRESSION_REQUIRED, "true");
		ex.getIn().setHeader(CFTConstants.CFT_DESTINATION_DIRECTORY, destDir);
		ex.getIn().setHeader(CFTConstants.CFT_DESTINATION_FILE, destFile);
		ex.getIn().setHeader(CFTConstants.FC_JOB_JOB_ID, jobId);
		ex.getIn().setHeader(CFTConstants.CFT_SOURCE_DIRECTORY, SOURCE_DIR);
		ex.getIn().setHeader(CFTConstants.CFT_SOURCE_FILE, srcFile);
		ex.getIn().setHeader(CFTConstants.PLUGIN_NAME, pluginName);
		return ex;
	}

	@Override
	protected CamelContext createCamelContext() throws Exception {
		final JndiContext ctx = new JndiContext();
		ctx.bind("sftpPool", sftpPool);
		return new DefaultCamelContext(ctx);
	}

	public CompositeFileCollectionJob createCFCJob(final String pluginName,
			final String destDir, final String destFile, final String jobId,
			final String srcFile) {

		final CompositeFileCollectionJob compositeJob = new CompositeFileCollectionJob();
		compositeJob.setDecompressionRequired(true);
		compositeJob.setDestinationDirectory(destDir);
		compositeJob.setDestinationFileName(destFile);
		compositeJob.setJobId(jobId);
		compositeJob.setSourceDirectory(SOURCE_DIR);
		compositeJob.setSourceFileName(srcFile);
		compositeJob.setPluginName(pluginName);
		return compositeJob;
	}

	@Test
	public void testProcess_ForWrongUserName_CustomCftExceptionIsThrown()
			throws Exception {

		final MockEndpoint mock = getMockEndpoint("mock:error");

		final Route route = this.context.getRoutes().get(0);
		final Endpoint start = route.getEndpoint();
		final Exchange exchange = getExchange(
				start,
				"com.ericsson.nms.umts.ranos.pms.collectionplugins.uetrace.UeTraceJobPlugin",
				"src/test/resources/segment1/UETRACE_PRE",
				"",
				"SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
				"");
		exchange.getIn().setHeader(CFTConstants.CFT_TARGET_PASSWORD, USERNAME);
		final Producer producer = start.createProducer();
		producer.process(exchange);

		mock.expectedMinimumMessageCount(1);
		final CustomCftException exception = (CustomCftException) exchange
				.getProperty(Exchange.EXCEPTION_CAUGHT);

		log.debug(exception.getErrorCode() + " : "
				+ exception.getErrorDescription());
		assertEquals(0, exception.getErrorCode());
		assertEquals(
				"Connection could not be established with Network Element due to "
						+ CFTConstants.JSchErrorMessages.AUTH_FAIL
								.getReplyMsg(),
				exception.getErrorDescription());
		mock.expectedMinimumMessageCount(1);
		assertMockEndpointsSatisfied();
	}

	@Test
	public void testProcess_ForWrongHostAddress_CustomCftExceptionIsThrown()
			throws Exception {

		final MockEndpoint mock = getMockEndpoint("mock:error");

		final Route route = this.context.getRoutes().get(0);
		final Endpoint start = route.getEndpoint();
		final Exchange exchange = getExchange(
				start,
				"com.ericsson.nms.umts.ranos.pms.collectionplugins.uetrace.UeTraceJobPlugin",
				"src/test/resources/segment1/UETRACE_PRE",
				"",
				"SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
				"");
		exchange.getIn().setHeader(CFTConstants.CFT_TARGET_IP_ADDRESS,
				"Non_localhost");
		final Producer producer = start.createProducer();
		producer.process(exchange);

		mock.expectedMinimumMessageCount(1);
		final CustomCftException exception = (CustomCftException) exchange
				.getProperty(Exchange.EXCEPTION_CAUGHT);

		assertEquals(9, exception.getErrorCode());
		assertEquals(
				"Connection could not be established with Network Element due to "
						+ CFTConstants.JSchErrorMessages.UNKNOWN_HOST_EXCEPTION
								.getReplyMsg(),
				exception.getErrorDescription());
		mock.expectedMinimumMessageCount(1);
		assertMockEndpointsSatisfied();
	}

	@Test
	public void testProcess_ForNullUserName_CustomCftExceptionIsThrown()
			throws Exception {

		final MockEndpoint mock = getMockEndpoint("mock:error");

		final Route route = this.context.getRoutes().get(0);
		final Endpoint start = route.getEndpoint();
		final Exchange exchange = getExchange(
				start,
				"com.ericsson.nms.umts.ranos.pms.collectionplugins.uetrace.UeTraceJobPlugin",
				"src/test/resources/segment1/UETRACE_PRE",
				"",
				"SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
				"");
		exchange.getIn().setHeader(CFTConstants.CFT_TARGET_USERNAME, null);
		final Producer producer = start.createProducer();
		producer.process(exchange);

		mock.expectedMinimumMessageCount(1);
		final CustomCftException exception = (CustomCftException) exchange
				.getProperty(Exchange.EXCEPTION_CAUGHT);

		assertEquals(0, exception.getErrorCode());
		assertEquals(
				"Connection could not be established with Network Element due to "
						+ CFTConstants.JSchErrorMessages.USER_NAME_NOT_NULL
								.getReplyMsg(),
				exception.getErrorDescription());
		mock.expectedMinimumMessageCount(1);
		assertMockEndpointsSatisfied();
	}

	@Test
	public void testProcess_ForWrongPluginClassName_CustomCftExceptionIsThrown()
			throws Exception {

		final MockEndpoint mock = getMockEndpoint("mock:error");

		final Route route = this.context.getRoutes().get(0);
		final Endpoint start = route.getEndpoint();
		final Exchange exchange = getExchange(
				start,
				"wrong.class.name",
				"src/test/resources/segment1/UETRACE_PRE",
				"",
				"SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
				"");
		final Producer producer = start.createProducer();
		producer.process(exchange);

		mock.expectedMinimumMessageCount(1);
		final CustomCftException exception = (CustomCftException) exchange
				.getProperty(Exchange.EXCEPTION_CAUGHT);

		assertEquals(-1, exception.getErrorCode());
		assertStringContains(exception.getErrorDescription(),
				"java.lang.ClassNotFoundException: wrong.class.name");
		mock.expectedMinimumMessageCount(1);
		assertMockEndpointsSatisfied();
	}

	@Test
	public void testProcess_ForWrongPluginClassNameNull_CustomCftExceptionIsThrown()
			throws Exception {

		final MockEndpoint mock = getMockEndpoint("mock:error");

		final Route route = this.context.getRoutes().get(0);
		final Endpoint start = route.getEndpoint();
		final Exchange exchange = getExchange(
				start,
				null,
				"src/test/resources/segment1/UETRACE_PRE",
				"",
				"SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-1,MeContext=ERBSM_12B00001::UE_TRACE::1::1361961900000",
				"");
		final Producer producer = start.createProducer();
		producer.process(exchange);

		mock.expectedMinimumMessageCount(1);
		final CustomCftException exception = (CustomCftException) exchange
				.getProperty(Exchange.EXCEPTION_CAUGHT);

		assertEquals(-1, exception.getErrorCode());
		assertStringContains(exception.getErrorDescription(),
				"Plugin class name is not valid");
		mock.expectedMinimumMessageCount(1);
		assertMockEndpointsSatisfied();
	}
}
