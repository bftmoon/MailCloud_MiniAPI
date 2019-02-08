package com.nodo.mailcloud_miniapi.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Dispatche extends JsonResponse<Dispatche.DispatcherBody> {

    public ShareUrlInfo urlGet() {
        return body().get.get(0);
    }

    public ShareUrlInfo urlUpload() {
        return body().upload.get(0);
    }

    @Override
    public String toString() {
        return "{get: {count: " + body.get.get(0).count + ", url: " + urlGet() + "},\n" +
                "upload: {count " + body.upload.get(0).count + ", url: " + urlUpload() + "}";
    }

    class DispatcherBody {
        @SerializedName("get")
        @Expose
        List<ShareUrlInfo> get;
        @SerializedName("upload")
        @Expose
        List<ShareUrlInfo> upload;
    }
}
