package com.nodo.mailcloud_miniapi.json;

import com.google.gson.annotations.SerializedName;

public class ShareUrlInfo {

    @SerializedName("count")
    int count;
    @SerializedName("url")
    private
    String url;

    public int getCount() {
        return count;
    }

    public String getUrl() {
        return url;
    }
}
