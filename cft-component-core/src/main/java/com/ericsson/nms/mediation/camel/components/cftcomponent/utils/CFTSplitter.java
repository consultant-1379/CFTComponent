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
package com.ericsson.nms.mediation.camel.components.cftcomponent.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;

/**
 * Splitter bean used with CFTC component to split list of jobs into list of
 * individual messages sent down the route chain
 * 
 * @author edejket
 * 
 */
public class CFTSplitter {

    public List<Message> sendCollectionRequests(final @Body List<Object> jobs) {
        final List<Message> singleJobs = new ArrayList<Message>();
        for (Object job : jobs) {
            final DefaultMessage message = new DefaultMessage();
            message.setBody(job);
            singleJobs.add(message);
        }
        return singleJobs;
    }
}
