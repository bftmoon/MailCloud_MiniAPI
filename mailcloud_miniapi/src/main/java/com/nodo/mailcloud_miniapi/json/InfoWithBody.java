package com.nodo.mailcloud_miniapi.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class InfoWithBody extends JsonResponse<JsonObject> {

    public FolderInfo deserializeToFolderInfo() {
        return new GsonBuilder()
                .registerTypeAdapter(EntriesLists.class, new EntriesListDeserializer())
                .create()
                .fromJson(body, FolderInfo.class);
    }

    public FileInfo deserializeToFileInfo() {
        return new Gson().fromJson(body, FileInfo.class);
    }
}
