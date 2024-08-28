/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.filetransferhandler.filetransfer.filetransferdetails;

import java.io.Serializable;

/**
 * This class contains node information that file transfer handler will be using
 * to create FTP/SFTP session towards node.
 * 
 * @author kamkaka
 * 
 */
public class NodeInfo implements Serializable {

    public static final long serialVersionUID = -9098142964463921860L;

    public static final String DEFAULT_TIME_ZONE = "TZERR";

    public String nodeJobId = "";

    /**
     * Node fdn is needed to get node's credential from TSS.
     */
    public String node_fdn;

    /**
     * If node security set to "ON" file will be collected through SFTP else
     * with FTP.
     */
    public String node_security;

    /**
     * Node IP address
     */
    public String node_ip;

    /**
     * Node name e.g MeContext=RNC01 so node name will RNC01
     */
    public String node_mecontext_id;

    /**
     * Node Connection Status
     */
    public boolean is_node_connected;

    public String timeZone = DEFAULT_TIME_ZONE;

    /**
     * Is node supported by OSS.
     */
    public boolean is_node_supported;

    public int node_type;

    @Override
    public int hashCode() {
        return node_fdn.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return node_fdn.equalsIgnoreCase(((NodeInfo) obj).node_fdn);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Node Details {");
        builder.append("; Node FDN = ").append(node_fdn);
        builder.append("; Node MeContext ID = ").append(node_mecontext_id);
        builder.append("; Node IP adress = ").append(node_ip);
        builder.append("; Node Security = ").append(node_security);
        builder.append("; Node Type = ").append(node_type);
        return builder.toString();
    }

    /**
     * Get the timezone for this node.
     * 
     * @return String the timezone for this node
     */
    public String getNodeTimeZone() {
        return timeZone;
    }

    public void setNodeTimeZone(final String inTimeZone) {
        timeZone = inTimeZone;
    }

    public String getNode_fdn() {
        return node_fdn;
    }

}
