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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.mediation.camel.components.cftcomponent.CftEndpoint;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.nms.mediation.camel.components.cftcomponent.utils.CFTUtils;
import com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface.ftpsessionops.FTPSessionOperations;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.ericsson.oss.mediation.camel.components.eftp.pool.*;
import com.jcraft.jsch.ChannelSftp;

/**
 * This class will provide the functionality of ftp session towards the NE. It
 * will abstract the behavior of ftp or sftp to facilitate the plug-in in
 * getting the remote NE directory listing or collecting files.
 * 
 * @author esersla
 * 
 */
public class FTPSessionImpl implements FTPSessionOperations { // NOPMD

	private static final transient Logger LOG = LoggerFactory
			.getLogger(FTPSessionImpl.class);

	/**
	 * when node security state is non-secure then a simple FTP connection will
	 * be created towards node in that case <code>FtpConnectionPool</code> be
	 * used to create connection with node
	 */
	private FTPClient ftpClient = null;

	/**
	 * in case of node being secure SSH FTP connection will created towards the
	 * node using <code>SftpConnectionPool</code>
	 */
	private ChannelSftp channel = null;

	public FTPSessionImpl(final ConnectionConfig key, final CftEndpoint endpoint)
			throws CompositeFTPException {
		borrowSession(key, endpoint);
	}

	/**
	 * This method will interact with Connection Pools & borrow the connection
	 * object based on the security state of NE
	 * 
	 * @throws CompositeFTPException
	 *             if any exception occurred during borrow operation from
	 *             connection pool then <code>CompositeFTPException</code> will
	 *             thrown back to stop further processing of in the current
	 *             composite file collection operation
	 */
	private void borrowSession(final ConnectionConfig key,
			final CftEndpoint endpoint) throws CompositeFTPException {
		try {
			LOG.debug(
					" Going to fetch connection from connection pool for composite file collection job config {}",
					key);
			if (key.getSecure() != null && Boolean.valueOf(key.getSecure())) {
				channel = (ChannelSftp) obtainSftpPoolReference(endpoint)
						.borrowObject(key);
				LOG.debug(" fetched a sftp connection for composite file collection job");
			} else {
				LOG.debug(
						"going to fetch ftp connection from pool with config {} & endpoint {}",
						new Object[] { key, endpoint });
				ftpClient =  obtainFtpPoolReference(endpoint).borrowObject(key);

				LOG.debug(" fetched a ftp connection for composite file collection job");
			}
		} catch (final Exception jschException) {
			LOG.error(" Exception occured while borrowSession() is {}",
					jschException);
			CFTUtils.resolveJschErrorCodes(jschException);
			throw new CompositeFTPException(
					"0: Connection could not be establiched with the node ");
		}
	}

	/**
	 * Obtain reference to sftp pool
	 * @param endpoint to get camel context
	 * @return FTPConnectionPool
	 */
	public SftpConnectionPool obtainSftpPoolReference(final CftEndpoint endpoint) {
		return (SftpConnectionPool) endpoint.getCamelContext().getRegistry()
				.lookup(Constants.SFTP_POOL);
	}
	
	/**
	 * Obtain reference to ftp pool
	 * @param endpoint endpoint to get camel context
	 * @return FTPConnectionPool
	 */
	public FtpConnectionPool obtainFtpPoolReference(final CftEndpoint endpoint) {
		return (FtpConnectionPool) endpoint.getCamelContext().getRegistry()
				.lookup(Constants.FTP_POOL);
	}

	/**
	 * this API is used by Plug-in classes to perform the listing operation on
	 * Network Element
	 * 
	 * @param dirname
	 *            the path on network element where listing has to be done
	 * @param filenameFilter
	 *            array of String containing filters and file names matching
	 *            these filters will only be collected
	 */
	@Override
	public Collection<String> getDirListing(final String dirname,
			final String[] filenameFilter) throws CompositeFTPException {
		Collection<String> fileList = null;

		LOG.debug(" starting remote directory listing for path " + dirname);
		if (ftpClient != null) {
			fileList = getFTPClientOperation().useFTPListing(dirname,
					filenameFilter, ftpClient);
		} else if (channel != null) {
			fileList = getSFTPClientOperation().useSftpListing(dirname,
					filenameFilter, channel);
		} else {
			LOG.error(" Session is no longer valid ");
		}
		// Return empty-list when not connected
		final Collection<String> dirList = fileList == null ? new ArrayList<String>(
				0) : fileList;
		LOG.debug("directory listing resulted in number of files : {}",
				dirList.size());
		return dirList;
	}

	/**
	 * This API will be used by Plug-in classes to collect pre or post processed
	 * composite files
	 * 
	 */
	@Override
	public boolean collectFile(
			final SingleFileTransferInfo singlefiletransferinfo)
			throws CompositeFTPException {
		LOG.debug(" collectFile() : FTPSessionImpl invoked for file {}",
				singlefiletransferinfo.oss_file_name);
		checkOssFolderAndCreateItIfNeeded(singlefiletransferinfo.oss_path);
		if (ftpClient != null) {
			return getFTPClientOperation().useFTPCollect(
					singlefiletransferinfo, ftpClient);
		} else if (channel != null) {
			return getSFTPClientOperation().useSftpCollect(
					singlefiletransferinfo, channel);
		}
		return false;
	}

	/**
	 * Check OSS folder. Create folder (and sub-folders) if needed. Throw
	 * exception if permission problems
	 * 
	 * @param destPath
	 *            the destination folder that needs to checked
	 * @throws CompositeFTPException
	 */
	public void checkOssFolderAndCreateItIfNeeded(final String destPath)
			throws CompositeFTPException {

		final File theDir = new File(destPath);
		LOG.debug(
				" checking destination folder {} exists or not, otherwise create it",
				destPath);
		if (!theDir.exists()) {
			theDir.mkdirs();
			if (!theDir.exists()) {
				handleDestPathWriteProblems(
						"Could not make local directory for storing of fetched file.",
						destPath);
			}
		}
		if (!theDir.canWrite()) {
			handleDestPathWriteProblems(
					"Could not write to local directory of fetched file.",
					destPath);
		}
	}

	/**
	 * If the destination folder is not writable
	 * 
	 * @param message
	 *            error message representing the problem
	 * @param destPath
	 *            the destination folder which could not be written into
	 * @throws CompositeFTPException
	 *             throws exception in order to stop rest of the sub-jobs in
	 *             this composite operation
	 */
	public static void handleDestPathWriteProblems(final String message,
			final String destPath) throws CompositeFTPException {
		final String errorMessage = message + " Local Path is: " + destPath;
		LOG.error(errorMessage);
		throw new CompositeFTPException(errorMessage);
	}

	/**
	 * @return SFTPClientOperation instance
	 */
	protected SFTPClientOperation getSFTPClientOperation() {
		return new SFTPClientOperation();
	}

	/**
	 * @return FTPClientOperation instance
	 */
	protected FTPClientOperation getFTPClientOperation() {
		return new FTPClientOperation();
	}

	/**
	 * @return the ftpClient
	 */
	public FTPClient getFtpClient() {
		return ftpClient;
	}

	/**
	 * @param ftpClient the ftpClient to set
	 */
	public void setFtpClient(final FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}

	/**
	 * @return the channel
	 */
	public ChannelSftp getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(final ChannelSftp channel) {
		this.channel = channel;
	}

}
