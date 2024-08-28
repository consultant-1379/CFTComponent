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
package com.ericsson.nms.mediation.camel.components.cftcomponent.constants;

import java.io.File;
import java.io.FilenameFilter;

public class CFTJarFilter implements FilenameFilter {
    public boolean accept(final File file, final String s) {
        final String s1 = s.toLowerCase();
        return s1.endsWith(".jar") || s1.endsWith(".zip");
    }
}
