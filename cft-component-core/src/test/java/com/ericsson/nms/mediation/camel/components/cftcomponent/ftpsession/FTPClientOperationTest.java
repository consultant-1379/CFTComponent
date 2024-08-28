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
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CompositeFTPException;
import com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails.SingleFileTransferInfo;

@RunWith(MockitoJUnitRunner.class)
public class FTPClientOperationTest {

    /**
     * filter to be used in the listing of directory
     */
    private static final String[] FILENAME_FILTER = new String[] { "_1",
            "_uetrace_" };

    private static final String FILE_NAME = "src/test/resources/TestFileName";

    @Mock
    FTPClient ftpClient;

    @Mock
    SingleFileTransferInfo sfti;

    @Mock
    FTPFile ftpfile;

    @Mock
    FTPFile ftpfile_1;

    @Mock
    Closeable stream_1;

    @Mock
    Closeable stream_2;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    FTPClientOperation ftp;

    @Before
    public void setup() {
        ftp = new FTPClientOperation();
        when(sfti.getOssFullFileName()).thenReturn(FILE_NAME);
    }

    @Test
    public void testUseFTPCollect() throws Exception {
        when(
                ftpClient.retrieveFile(any(String.class),
                        any(FileOutputStream.class))).thenReturn(true);
        assertTrue(ftp.useFTPCollect(sfti, ftpClient));
    }

    @Test
    public void testUseFTPListing() throws Exception {
        final String filename_1 = "A20130227.1415-1430_1.xml.gz";
        final String filename_2 = "A20130227.1030-1045_uetrace_45604501001a.bin.gz";
        Collection<String> expectedlist = new ArrayList<String>();
        expectedlist.add(filename_1);
        expectedlist.add(filename_2);

        when(ftpfile.getName()).thenReturn(filename_1);
        when(ftpfile_1.getName()).thenReturn(filename_2);
        when(ftpClient.listFiles(any(String.class))).thenReturn(
                new FTPFile[] { ftpfile, ftpfile_1 });

        Collection<String> filelist = ftp.useFTPListing("", FILENAME_FILTER,
                ftpClient);

        assertTrue(filelist.containsAll(expectedlist));
    }

    @Test
    public void connect_FailedUseFTPCollect_ThrowsCompositeFTPExceptionWithCorrectErrorCode()
            throws Exception {
        when(ftpClient.getReplyCode()).thenReturn(550);
        when(ftpClient.getReplyString()).thenReturn("file not available");
        when(
                ftpClient.retrieveFile(any(String.class),
                        any(FileOutputStream.class))).thenThrow(
                new IOException());
        exception.expect(CompositeFTPException.class);
        ftp.useFTPCollect(sfti, ftpClient);
    }

    @Test
    public void connect_FailedUseFTPListing_ThrowsCompositeFTPExceptionWithCorrectErrorCode()
            throws Exception {
        when(ftpClient.getReplyCode()).thenReturn(530);
        final String dirname = "/c/pm_data/";
        final String errmsg = "directory not available";
        when(ftpClient.getReplyString()).thenReturn(
                "Directory listing of: " + dirname + " has failed. Reason "
                        + errmsg);
        when(ftpClient.listFiles(any(String.class))).thenThrow(
                new IOException());
        exception.expect(CompositeFTPException.class);
        ftp.useFTPListing(dirname, FILENAME_FILTER, ftpClient);
    }

    @Test
    public void testCloseDataStreams() throws Exception {
        ftp.closeDataStreams(stream_1, stream_2);
        verify(stream_1, times(1)).close();
        verify(stream_2, times(1)).close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        final File tempfile = new File(FILE_NAME);
        if (tempfile.exists()) {
            tempfile.deleteOnExit();
        }
    }
}
