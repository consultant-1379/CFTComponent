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

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.mediation.camel.components.cftcomponent.constants.CFTConstants;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CustomCftException;
import com.ericsson.nms.mediation.camel.components.cftcomponent.ftpsession.FTPSessionImpl;
import com.ericsson.nms.mediation.camel.components.cftcomponent.processor.CFTProcessor;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.*;
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.FTPMPluginFactoryInterface;
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.ftpsessionops.FTPSessionOperations;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.ericsson.oss.mediation.camel.components.eftp.pool.*;
import com.ericsson.oss.mediation.pm.model.*;
import com.jcraft.jsch.ChannelSftp;

/**
 * The CFTComponent producer. Its boilerplate class, which will instantiate the
 * plug-in class & call the necessary preProcess or postProcessor methods on
 * that plug-in. And also collects the list of files returned by these plug-in
 * processors.
 */
public class CftProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory
			.getLogger(CftProducer.class);

	private final CftEndpoint endpoint;

	public CftProducer(final CftEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	/**
	 * Its the entry point from Camel into this processor
	 * 
	 * @param exchange
	 *            {@link Exchange}
	 * 
	 * @throws CustomCftException
	 */
	@Override
	public void process(final Exchange exchange) throws CustomCftException {
		LOG.debug(" process() : CftProducer");
		ConnectionConfig config = null;
		FTPSessionOperations sessionOperations = null;
		List<SingleFileTransferInfo> filesToBeCollected = new ArrayList<SingleFileTransferInfo>();

		final String pluginClassName = (String) exchange.getIn().getHeader(
				CFTConstants.PLUGIN_NAME);
		FTPMPluginFactoryInterface pluginInstance = null;

		try {

			final CFTProcessor processor = new CFTProcessor();

			pluginInstance = CFTUtils.instantiateClass(pluginClassName,
					getEndpoint());

			config = getConfigKey(exchange);
			sessionOperations = createFTPSession(config);

			final SingleFileTransferInfo firstjob = SingleFileTransferInfoFactory
					.createSFTInfo(exchange);
			filesToBeCollected.add(firstjob);

			filesToBeCollected = processor.preProcessCollectionJob(firstjob,
					pluginInstance, filesToBeCollected, sessionOperations);

			final FileCollectionResult result = processor.collectFile(
					filesToBeCollected, sessionOperations);
			if (result != null && result instanceof FileCollectionSuccess) {

				filesToBeCollected = processor.postProcessCollectionJob(
						firstjob, pluginInstance, filesToBeCollected,
						sessionOperations);
			}

			LOG.debug(
					" Total {} FileCollectionJob created from composite job {}",
					new Object[] { filesToBeCollected.size(), firstjob });

			exchange.getIn().setBody(
					createFileCollectionJobList(filesToBeCollected, result));
		} catch (final CompositeFTPException e) {
			LOG.error(
					"Error occured while executing composite file collection job, stack trace {}",
					e);
			throw new CustomCftException(e.getErrorCode(), e.getMessage());
		} catch (final Exception e) {
			LOG.error(
					"Error occured while executing composite file collection job, stack trace {}",
					e);
			throw e;
		} finally {
			LOG.debug(" returning the connection to pool");
			returnConnectionToPool((FTPSessionImpl) sessionOperations, config);
			LOG.debug(" connection successfully returned to pool");
		}
	}

	/**
	 * @param filesToBeCollected
	 * @param result
	 * @return
	 */
	private List<Object> createFileCollectionJobList(
			final List<SingleFileTransferInfo> filesToBeCollected,
			final FileCollectionResult result) {
		final List<Object> fcJobs = new ArrayList<>();
		for (final SingleFileTransferInfo singleFTJob : filesToBeCollected) {
			final FileCollectionJob filecollectionjob = CFTResponsesFactory
					.createFCJob(singleFTJob);
			fcJobs.add(filecollectionjob);
		}
		if (result != null) {
			fcJobs.add(result);
		}
		return fcJobs;
	}

	/**
	 * @param config
	 * @throws Exception
	 */
	private void returnConnectionToPool(final FTPSessionImpl ftpSession,
			final ConnectionConfig key) throws CustomCftException {
		try {
			if (null != key && ftpSession != null) {
				if ("true".equalsIgnoreCase(key.getSecure())) {
					final SftpConnectionPool pool = ftpSession
							.obtainSftpPoolReference(endpoint);
					final ChannelSftp channel = ftpSession.getChannel();
					if (channel.getSession().isConnected()) {
						LOG.debug(
								"CFTP::returnSFTPConnection [{}], session is connected...",
								key);
						if (channel.isConnected() && !channel.isClosed()) {
							LOG.debug(
									"CFTP::returnSFTPConnection [{}], channel is connected and is not closed...",
									key);
							pool.returnObject(key, channel);
							LOG.debug(
									"CFTP::returnSFTPConnection [{}], returned channel into pool.",
									key);
						} else {
							LOG.debug(
									"CFTP::returnSFTPConnection invalidating [{}], channel is either discconected or closed.",
									key);
							pool.invalidateObject(key, channel);
							LOG.debug("CFTP::returnSFTPConnection, invalidated [{}]",
									key);
						}
					} else {
						LOG.debug("returnSFTPConnection, session associatied with this channel is not connected, invalidating key [{}]",key);
						pool.invalidateObject(key, channel);
					}

				} else {
					ftpSession
					.obtainFtpPoolReference(endpoint).returnObject(key,
							ftpSession.getFtpClient());

				}
			}
		} catch (final Exception e) {
			LOG.error(
					"Error occured while returning connection to the connection pool, stack trace {}",
					e);
			throw new CustomCftException(-1, e.getLocalizedMessage());
		}
	}

	/**
	 * 
	 * @param exchange
	 * @return
	 * @throws CompositeFTPException
	 */
	private ConnectionConfig getConfigKey(final Exchange exchange)
			throws CompositeFTPException {

		final String ipAddress = (String) exchange.getIn().getHeader(
				CFTConstants.CFT_TARGET_IP_ADDRESS);

		final Integer port = Integer.parseInt(exchange.getIn()
				.getHeader(CFTConstants.CFT_TARGET_PORT).toString());

		final String username = (String) exchange.getIn().getHeader(
				CFTConstants.CFT_TARGET_USERNAME);

		final String password = (String) exchange.getIn().getHeader(
				CFTConstants.CFT_TARGET_PASSWORD);

		final String secure = (String) exchange.getIn().getHeader(
				CFTConstants.CFT_SECURE_FTP);

		return new ConnectionConfig(ipAddress, port, username, password, secure);
	}

	/**
	 * to get the implementation of <code>FTPSessionOperation</code> It will be
	 * Facade for the camel end-point providing functionality of <i>getting
	 * remote file</i> & NE <i>directory listing</i>
	 * 
	 * @return instance of <code>FTPSessionOperation</code>
	 * @throws CompositeFTPException
	 */
	protected FTPSessionOperations createFTPSession(final ConnectionConfig key)
			throws CompositeFTPException {
		final FTPSessionImpl ftpSession = new FTPSessionImpl(key,
				this.getEndpoint());
		return ftpSession;
	}

	/**
	 * @return the endpoint
	 */
	@Override
	public CftEndpoint getEndpoint() {
		return endpoint;
	}

}
