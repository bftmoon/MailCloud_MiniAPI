package com.nodo.mailcloud_miniapi.json;

import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class FileInfo {
    @SerializedName("mtime")
    private
    Long mtime;
    @SerializedName("name")
    private
    String name;
    @SerializedName("size")
    private
    Long size;
    @SerializedName("home")
    private
    String home;
    @SerializedName("hash")
    private
    String hash;

    public Date getLastModifiedTimeUTC() {
        return new Date(mtime);
    }

    public long getLastModifiedTime() {
        return mtime;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getFullPath() {
        return home;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "{\nmtime: " + mtime + ",\n" +
                "name: " + name + ",\n" +
                "size: " + size + ",\n" +
                "home: " + home + "}";
    }
}
