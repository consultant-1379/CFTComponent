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

import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.nms.mediation.camel.components.cftcomponent.exceptions.CustomCftException;

/**
 * Represents a cftcomponent endpoint.
 */
public class CftEndpoint extends DefaultEndpoint {

    public static final Logger LOG = LoggerFactory.getLogger(CftEndpoint.class);


    private String ftPluginJarDirectory;

    /**
     * Default constructor
     * 
     */
    public CftEndpoint() {
        super();
    }

    public CftEndpoint(final String uri, final CftComponent component) {
        super(uri, component);
        LOG.debug("CftEndpoint constructor called ...");
        this.ftPluginJarDirectory = this.getEndpointConfiguration()
                .getParameter("ftPluginJarDirectory");
    }

    @Override
    public Producer createProducer() throws CustomCftException {
        try {
            return new CftProducer(this);
        } catch (Exception e) {
            throw new CustomCftException(-1, "Exception create producer: " + e);
        }
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Consumer createConsumer(final Processor arg0)
            throws CustomCftException {
        // We only support the "to" element in this component so we only need to
        // provide producers no consumers
        throw new CustomCftException(-1, "No Consumers for this component");
    }


    /**
     * @return the ftPluginJarDirectory
     */
    public String getFtPluginJarDirectory() {
        return ftPluginJarDirectory;
    }

    /**
     * @param ftPluginJarDirectory
     *            the ftPluginJarDirectory to set
     */
    public void setFtPluginJarDirectory(final String ftPluginJarDirectory) {
        this.ftPluginJarDirectory = ftPluginJarDirectory;
    }

    @Override
    public void stop() throws IllegalStateException {
        try {
            super.stop();
            LOG.debug("endpoint stop method called");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void start() throws IllegalStateException {
        try {
            super.start();
            LOG.debug("Endpoint start method called");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
