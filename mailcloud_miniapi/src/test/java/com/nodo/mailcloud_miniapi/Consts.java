package com.nodo.mailcloud_miniapi;

import static com.nodo.mailcloud_miniapi.MyConsts.TEST_RES_PATH;

class Consts {

    public static final String PC_DOWNLOAD_DIR = TEST_RES_PATH + "testing/download";
    public static final String PC_UPLOAD_DIR = TEST_RES_PATH + "testing/upload";

    public static final String CLOUD_DOWNLOAD_DIR = "/testing/download";
    public static final String CLOUD_UPLOAD_DIR = "/testing/upload";
    public static final String CLOUD_CREATE_DIR = "/testing/create";

    public static final String CLOUD_ADD_SYNC_DIR = "/testing/sync_add_all";
    public static final String CLOUD_DEL_SYNC_DIR = "/testing/sync_del_all";

    public static final String PC_ADD_SYNC_DIR = TEST_RES_PATH + "/testing/sync_add_all";
    public static final String PC_DEL_SYNC_DIR = TEST_RES_PATH + "/testing/sync_del_all";
    public static final String PC_RELOAD_SYNC = TEST_RES_PATH + "/testing/sync_reload";

    public static final String NETWORK_ON_COMMAND = "nmcli networking on";
    public static final String NETWORK_OFF_COMMAND = "nmcli networking off";

}
