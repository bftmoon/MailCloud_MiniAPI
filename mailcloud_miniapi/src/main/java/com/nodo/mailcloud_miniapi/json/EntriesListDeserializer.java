package com.nodo.mailcloud_miniapi.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

class EntriesListDeserializer implements JsonDeserializer<EntriesLists> {

    @Override
    public EntriesLists deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        EntriesLists list = new EntriesLists();
        JsonArray array = json.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            JsonObject element = array.get(i).getAsJsonObject();
            String type = element.get("type").getAsString();
            if (type.equals("folder")) {
                list.folders.add(context.deserialize(element, FolderInfo.class));
            } else if (type.equals("file"))
                list.files.add(context.deserialize(element, FileInfo.class));
        }
        return list;
    }
}
