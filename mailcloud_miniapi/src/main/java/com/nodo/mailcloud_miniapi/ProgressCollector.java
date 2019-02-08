package com.nodo.mailcloud_miniapi;

class ProgressCollector {

    private final CloudProgressListner listner;
    private int current_num,
            num_of_files = -1;

    public ProgressCollector(CloudProgressListner listner) {
        this.listner = listner;
        if (listner == null)
            throw new IllegalArgumentException("listner can't be null");
    }

    synchronized void sendConnectionError(String error_message) {
        listner.catchException(new MailCloudException(MailCloudException.Type.ConnectionError, error_message));
    }

    synchronized void sendException(int code, String error_message) {
        try {
            MailCloudException.checkExceptionForCode(code, error_message);
        } catch (MailCloudException e) {
            listner.catchException(e);
        }
    }

    synchronized void sendException(MailCloudException e) {
        listner.catchException(e);
    }

    synchronized void sendUnknownError(String error_message) {
        listner.catchException(new MailCloudException(MailCloudException.Type.Unknown, error_message));
    }

    synchronized void incrementCurrentNum() {
        listner.updateFilesProgress(++current_num);
    }

    synchronized void addToMaxNum(int num) {
        listner.setFilesMaxProgress(num_of_files += num);
    }

    void setNum_of_files(int num_of_files) {
        this.num_of_files = num_of_files;
        listner.setFilesMaxProgress(num_of_files);
    }

    boolean numOfFilesRequired() {
        return num_of_files == -1;
    }

    public void setDynamicMax() {
        if (num_of_files == -1)
            num_of_files = 0;
    }

    public void resetNums() {
        current_num = 0;
        num_of_files = -1;

    }
}

