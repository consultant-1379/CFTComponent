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
package com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions;

import com.ericsson.oss.ftpmanager.common.filetransfer.exceptions.FTPErrorArea;
import com.ericsson.oss.ftpmanager.common.filetransfer.exceptions.FTPException;

/**
 * Custom exception thrown if any FTP/SFTP operation fails
 * 
 * @author esersla
 * 
 */
public class CompositeFTPException extends FTPException {

    private static final long serialVersionUID = -2281598486090170878L;

    /**
     * @param fTPErrorArea
     *            by default it will be FTP, as its non-relevant here
     * @param message
     *            the error message
     */
    public CompositeFTPException(final String message) {
        super(FTPErrorArea.FTP, message);
    }

    public CompositeFTPException(final int errorCode, final String message) {
        super(FTPErrorArea.FTP, message);
        setErrorCode(errorCode);
    }

}
