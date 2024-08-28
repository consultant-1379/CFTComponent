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

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CustomCftException;

/**
 * Represents the component that manages {@link CftEndpoint}.
 */
public class CftComponent extends DefaultComponent {

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining,
            final Map<String, Object> parameters) throws CustomCftException {
        final Endpoint endpoint = new CftEndpoint(uri, this);
        try {
            setProperties(endpoint, parameters);
        } catch (Exception e) {
            throw new CustomCftException(-1, "SetProperty exception: " + e);
        }
        return endpoint;
    }
}
