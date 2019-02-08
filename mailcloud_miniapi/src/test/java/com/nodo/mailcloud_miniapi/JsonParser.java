package com.nodo.mailcloud_miniapi;

import android.util.Log;

import com.google.gson.Gson;
import com.nodo.mailcloud_miniapi.json.AuthToken;
import com.nodo.mailcloud_miniapi.json.Dispatche;
import com.nodo.mailcloud_miniapi.json.FileInfo;
import com.nodo.mailcloud_miniapi.json.FolderInfo;
import com.nodo.mailcloud_miniapi.json.InfoWithBody;

public class JsonParser {
    public static Object Parse(String response, JsonObjectType parseObject) {
        if (response == null || response.isEmpty()) {
            throw new IllegalArgumentException("Response text is null or empty.");
        }

        switch (parseObject) {
            case Token:
                System.out.println(response);
                AuthToken jToken = new Gson().fromJson(response, AuthToken.class);
                Log.v("JsonParser", jToken.toString());
                return jToken;
            case File:
                FileInfo file = new Gson().fromJson(response, InfoWithBody.class).deserializeToFileInfo();
                Log.v("JsonParser", file.toString());
                return file;
            case Folder:
                FolderInfo folder = new Gson().fromJson(response, InfoWithBody.class).deserializeToFolderInfo();
                Log.v("JsonParser", folder.toString());
                return folder;
            case Dispatcher:
                Dispatche dispatche = new Gson().fromJson(response, Dispatche.class);
                Log.v("JsonParser", dispatche.toString());
                return dispatche;
            default:
                return null;
        }

    }

    public enum JsonObjectType {Token, Folder, Dispatcher, File}
}
