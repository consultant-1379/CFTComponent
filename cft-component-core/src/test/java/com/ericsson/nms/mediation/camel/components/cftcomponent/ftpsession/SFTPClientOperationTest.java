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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

@RunWith(MockitoJUnitRunner.class)
public class SFTPClientOperationTest {

    /**
     * filter to be used in the listing of directory
     */
    private static final String[] FILENAME_FILTER = new String[] { "_1",
            "_uetrace_" };

    private static final String FILE_NAME = "src/test/resources/TestFileName";

    @Mock
    ChannelSftp channel;

    @Mock
    SingleFileTransferInfo sfti;

    @Mock
    ChannelSftp.LsEntry lsEntry_1;

    @Mock
    ChannelSftp.LsEntry lsEntry_2;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    SFTPClientOperation sftp;

    @Before
    public void setup() {
        sftp = new SFTPClientOperation();
        when(sfti.getNodeFullFileName()).thenReturn(FILE_NAME);
        when(sfti.getOssFullFileName()).thenReturn(FILE_NAME);
    }

    @Test
    public void testUseSftpCollect() throws Exception {
        assertTrue(sftp.useSftpCollect(sfti, channel));
    }

    @Test
    public void testUseFTPListing() throws Exception {

        final String filename_1 = "A20130227.1415-1430_1.xml.gz";
        final String filename_2 = "A20130227.1030-1045_uetrace_45604501001a.bin.gz";

        Collection<String> expectedlist = new ArrayList<String>();
        expectedlist.add(filename_1);
        expectedlist.add(filename_2);

        Vector<ChannelSftp.LsEntry> listedFiles = new Vector<ChannelSftp.LsEntry>();
        listedFiles.add(lsEntry_1);
        listedFiles.add(lsEntry_2);

        when(lsEntry_1.getFilename()).thenReturn(filename_1);
        when(lsEntry_2.getFilename()).thenReturn(filename_2);
        when(channel.ls(any(String.class))).thenReturn(listedFiles);

        Collection<String> filelist = sftp.useSftpListing("", FILENAME_FILTER,
                channel);

        assertTrue(filelist.containsAll(expectedlist));
    }

    @Test
    public void connect_FailedUseSftpListing_ThrowsCompositeFTPExceptionWithCorrectErrorCode()
            throws Exception {
        final String dirname = "/c/pm_data/";
        final String errmsg = "directory not available";
        when(channel.ls(any(String.class))).thenThrow(
                new SftpException(2, errmsg));
        exception.expect(CompositeFTPException.class);
        exception.expectMessage(errmsg);
        sftp.useSftpListing(dirname, FILENAME_FILTER, channel);
    }
}
