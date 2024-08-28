package com.ericsson.oss.ftpmanager.external.filetransferhandler.plugin.iface;

public interface FTPMPluginFactoryInterface {

    /**
     * Returns a concrete implementation of the PreProcessOperations interface.
     * Contains logic for doing the pre-processing of the job.
     */
    FTPMPreProcessInterface getPreProcessor();

    /**
     * Returns a concrete implementation of the PostProcessOperations interface.
     * Contains logic for doing the post-processing of the job.
     */
    FTPMPostProcessInterface getPostProcessor();

}
