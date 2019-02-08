package com.nodo.mailcloud_miniapi;

import com.nodo.mailcloud_miniapi.json.AuthToken;
import com.nodo.mailcloud_miniapi.json.Dispatche;
import com.nodo.mailcloud_miniapi.json.FileInfo;
import com.nodo.mailcloud_miniapi.json.FolderInfo;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonTests {

    @Test
    public void TestJSONTokenParse() throws IOException {
        String json = new String(Files.readAllBytes(Paths.get("/home/ghost/Загрузки/MailruCloudPlayer-master/app/src/main/resources/token.json")));
        Object jobject = JsonParser.Parse(json, JsonParser.JsonObjectType.Token);
        assertNotNull(jobject);
        AuthToken token = (AuthToken) jobject;
        assertEquals(token.getStatus(), 200);
        assertEquals(token.getToken(), "761jhpLhgR7sN6KumJdH8V6jMxtZMJvJ");
    }

    @Test
    public void TestJSONDispatcherParse() throws IOException {
        String json = new String(Files.readAllBytes(Paths.get("/home/ghost/Загрузки/MailruCloudPlayer-master/app/src/main/resources/dispatche.json")));
        Object jobject = JsonParser.Parse(json, JsonParser.JsonObjectType.Dispatcher);
        assertNotNull(jobject);
        Dispatche dispatche = (Dispatche) jobject;
        assertEquals(dispatche.urlGet().getUrl(), "https://cloclo18.datacloudmail.ru/get/");
        assertEquals(dispatche.urlUpload().getUrl(), "https://cloclo21-upload.cloud.mail.ru/upload/");
    }

    @Test
    public void TestJSONFileParse() throws IOException {
        String json = new String(Files.readAllBytes(Paths.get("/home/ghost/Загрузки/MailruCloudPlayer-master/app/src/main/resources/file.json")));
        Object jobject = JsonParser.Parse(json, JsonParser.JsonObjectType.File);
        assertNotNull(jobject);
        FileInfo fileInfo = (FileInfo) jobject;
        assertEquals(fileInfo.getName(), "test.txt");
        assertEquals(fileInfo.getSize(), 2);
    }

    @Test
    public void TestJSONFolderDeserialization() throws IOException {
        String json = new String(Files.readAllBytes(Paths.get("/home/ghost/Загрузки/MailruCloudPlayer-master/app/src/main/resources/folder.json")));
        Object jobject = JsonParser.Parse(json, JsonParser.JsonObjectType.Folder);
        assertNotNull(jobject);
        FolderInfo folder = (FolderInfo) jobject;
        assertNotNull(folder);
        assertEquals(folder.getFoldersCount(), 1);
        assertEquals(folder.getFolders().size(), 1);
        assertEquals(folder.getFiles().size(), 2);
    }
}
