package com.nodo.mailcloud_miniapi;

public class SyncParams {

    boolean delete_from_cloud;
    boolean delete_from_phone;
    boolean load_to_cloud;
    boolean load_to_phone;

    public SyncParams(boolean delete_from_cloud, boolean delete_from_phone, boolean load_to_cloud, boolean load_to_phone) {
        if (delete_from_cloud == load_to_phone && delete_from_cloud)
            throw new IllegalArgumentException("Files can't be loaded to phone and deleted from Cloud at the same time");
        if (delete_from_phone == load_to_cloud && load_to_cloud)
            throw new IllegalArgumentException("Files can't be loaded to Cloud and deleted from phone at the same time");
        this.delete_from_cloud = delete_from_cloud;
        this.delete_from_phone = delete_from_phone;
        this.load_to_cloud = load_to_cloud;
        this.load_to_phone = load_to_phone;
    }

    public boolean delFromCloud() {
        return delete_from_cloud;
    }

    public boolean delFromPhone() {
        return delete_from_phone;
    }

    public boolean loadToCloud() {
        return load_to_cloud;
    }

    public boolean loadToPhone() {
        return load_to_phone;
    }
}
