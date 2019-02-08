package com.nodo.mailcloud_miniapi.json;

import com.google.gson.annotations.SerializedName;

public class JsonResponse<T> {

    @SerializedName("body")
    T body;
    @SerializedName("status")
    private
    int status;

    public int getStatus() {
        return status;
    }

    T body() {
        return body;
    }

    @Override
    public String toString() {
        return "{\nbody:" + body + ", \nstatus: " + status + "}";
    }
}


