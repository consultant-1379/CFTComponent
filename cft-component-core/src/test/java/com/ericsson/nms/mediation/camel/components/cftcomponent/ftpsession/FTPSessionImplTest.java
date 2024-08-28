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
package com.ericsson.nms.mediation.camel.components.cftcomponent.ftpsession;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.nms.mediation.camel.components.cftcomponent.CftEndpoint;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.ericsson.oss.mediation.camel.components.eftp.pool.*;
import com.jcraft.jsch.*;

@RunWith(MockitoJUnitRunner.class)
public class FTPSessionImplTest {

	private final static String IP_ADDRESS = "127.0.0.1";
	private final static int PORT = 2221;
	private final static String USERNAME = "user";
	private final static String PASSWORD = "pass";
	private static String secure;
	private FTPSessionImpl ftpSession;
	private ConnectionConfig key;
	private String[] filenameFilter;

	@Mock
	FTPClient ftpClient;

	@Mock
	ChannelSftp channel;

	@Mock
	SftpConnectionPool sftpPool;

	@Mock
	FtpConnectionPool ftpPool;

	@Mock
	CftEndpoint endpoint;

	@Mock
	FTPClientOperation clientOp;

	@Mock
	CamelContext camelCTX;

	@Mock
	Registry registry;
	
	@Mock
	SFTPClientOperation sftpClientOp;

	@Before
	public void setUp() throws Exception {
		filenameFilter = new String[] { "filter" };
		when(endpoint.getCamelContext()).thenReturn(camelCTX);
		when(camelCTX.getRegistry()).thenReturn(registry);
		when(registry.lookup(Constants.SFTP_POOL)).thenReturn(sftpPool);
		when(registry.lookup(Constants.FTP_POOL)).thenReturn(ftpPool);
		when(sftpPool.borrowObject(any(ConnectionConfig.class))).thenReturn(
				channel);
		when(ftpPool.borrowObject(any(ConnectionConfig.class))).thenReturn(
				ftpClient);

	}

	@Test
	public void borrowSessionForSftpTest() throws Exception {

		secure = "true";
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		ftpSession = new FTPSessionImpl(key, endpoint);
		Assert.assertNotNull(ftpSession.getChannel());

	}

	@Test
	public void borrowSessionForSftpThrowsExceptionTest() throws Exception {

		secure = "true";
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		when(sftpPool.borrowObject(any(ConnectionConfig.class))).thenThrow(
				new JSchException("UnknownHostException"));

		try {
			ftpSession = new FTPSessionImpl(key, endpoint);
			fail("Fail cause exception wasn't thrown");
		} catch (final CompositeFTPException expectedException) {
			assertEquals(9, expectedException.getErrorCode());
			assertTrue(expectedException.getMessage().contains(
					"remote host not avaialble for sftp connection"));
		}
	}

	/**
	 * This test needs to be reconsidered
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void borrowSessionForFtpTest() throws Exception {

		secure = "false";
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		ftpSession = new FTPSessionImpl(key, endpoint);
		FTPSessionImpl spy = spy( new FTPSessionImpl(key, endpoint));
		spy.obtainFtpPoolReference(endpoint);
		verify(spy, times(1)).obtainFtpPoolReference(endpoint);

	}

	@Test
	public void getDirectoryListingForFtpTest() throws Exception {

		secure = "false";
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		final FTPSessionImpl ftpSession = spy(new FTPSessionImpl(key, endpoint));
		final Collection<String> list = new ArrayList<>();
		list.add("first");
		when(ftpSession.getFTPClientOperation()).thenReturn(clientOp);
		when(clientOp.useFTPListing("", filenameFilter, ftpClient)).thenReturn(
				list);
		assertEquals(1, ftpSession.getDirListing("", filenameFilter).size());

	}

	@Test
	public void getDirectoryListingForSftpTest() throws Exception {

		secure = "true";
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		final FTPSessionImpl ftpSession = spy(new FTPSessionImpl(key, endpoint));
		final Collection<String> list = new ArrayList<>();
		list.add("first");
		list.add("second");
		when(ftpSession.getSFTPClientOperation()).thenReturn(sftpClientOp);
		when(sftpClientOp.useSftpListing("", filenameFilter, channel))
				.thenReturn(list);

		assertEquals(2, ftpSession.getDirListing("", filenameFilter).size());

	}

	@Test
	public void getDirectoryListingWhenNotConnectedTest() throws Exception {

		secure = "false";
		when(ftpPool.borrowObject(any(ConnectionConfig.class)))
				.thenReturn(null);
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		final FTPSessionImpl ftpSession = spy(new FTPSessionImpl(key, endpoint));
		final Collection<String> list = new ArrayList<>();
		list.add("first");
		when(ftpSession.getFTPClientOperation()).thenReturn(clientOp);
		when(clientOp.useFTPListing("", filenameFilter, ftpClient)).thenReturn(
				list);
		when(ftpSession.getSFTPClientOperation()).thenReturn(sftpClientOp);
		when(sftpClientOp.useSftpListing("", filenameFilter, channel))
				.thenReturn(list);
		assertEquals(0, ftpSession.getDirListing("", filenameFilter).size());

	}

	@Test
	public void collectFileForFTPTest() throws Exception {

		secure = "false";
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		final SingleFileTransferInfo singlefiletransferinfo = new SingleFileTransferInfo();
		singlefiletransferinfo.transfer_id = "test";
		final FTPSessionImpl ftpSession = spy(new FTPSessionImpl(key, endpoint));
		doNothing().when(ftpSession).checkOssFolderAndCreateItIfNeeded(
				singlefiletransferinfo.oss_path);
		when(ftpSession.getFTPClientOperation()).thenReturn(clientOp);
		when(clientOp.useFTPCollect(singlefiletransferinfo, ftpClient))
				.thenReturn(true);
		assertTrue(ftpSession.collectFile(singlefiletransferinfo));

		verify(ftpSession, times(1)).getFTPClientOperation();
		verify(clientOp, times(1)).useFTPCollect(singlefiletransferinfo,
				ftpClient

		);

	}

	@Test
	public void collectFileForSFTPTest() throws Exception {

		secure = "true";
		key = new ConnectionConfig(IP_ADDRESS, PORT, USERNAME, PASSWORD, secure);
		final SingleFileTransferInfo singlefiletransferinfo = new SingleFileTransferInfo();
		singlefiletransferinfo.transfer_id = "test";
		final FTPSessionImpl ftpSession = spy(new FTPSessionImpl(key, endpoint));
		doNothing().when(ftpSession).checkOssFolderAndCreateItIfNeeded(
				singlefiletransferinfo.oss_path);
		when(ftpSession.getSFTPClientOperation()).thenReturn(sftpClientOp);
		when(sftpClientOp.useSftpCollect(singlefiletransferinfo, channel))
				.thenReturn(true);
		assertTrue(ftpSession.collectFile(singlefiletransferinfo));

		verify(ftpSession, times(1)).getSFTPClientOperation();
		verify(sftpClientOp, times(1)).useSftpCollect(singlefiletransferinfo,
				channel

		);

	}
}
