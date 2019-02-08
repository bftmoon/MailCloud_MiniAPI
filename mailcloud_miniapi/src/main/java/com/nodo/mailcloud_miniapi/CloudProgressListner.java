package com.nodo.mailcloud_miniapi;

interface CloudProgressListner {

    /**
     * Increment num when action ends successful
     */
    void updateFilesProgress(int current_num);

    /**
     * Send num of all future actions. Not work with simpleSync()
     */
    void setFilesMaxProgress(int max_num);

    /**
     * Exception handler
     */
    void catchException(MailCloudException e);

}
