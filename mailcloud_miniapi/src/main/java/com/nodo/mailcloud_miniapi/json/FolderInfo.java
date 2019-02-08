package com.nodo.mailcloud_miniapi.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FolderInfo {

    @SerializedName("count")
    @Expose
    private
    EntriesCount count;
    @Expose
    @SerializedName("name")
    private
    String name;
    @Expose
    @SerializedName("size")
    private
    Long size;
    @Expose
    @SerializedName("home")
    private
    String home;
    @Expose
    @SerializedName("list")
    private
    EntriesLists list;

    public String getFullpath() {
        return home;
    }

    public int getFoldersCount() {
        return count.folders;
    }

    public int getFilesCount() {
        return count.files;
    }

    public int getContentCount() {
        return count.files + count.folders;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public List<FolderInfo> getFolders() {
        return list == null ? null : list.folders;
    }

    public List<FileInfo> getFiles() {
        return list == null ? null : list.files;
    }

    public ArrayList<String> getFilenames() {
        ArrayList<String> res = new ArrayList<>(count.files);
        for (FileInfo fileInfo : list.files)
            res.add(fileInfo.getName());
        return res;
    }

    public ArrayList<String> getFoldernames() {
        ArrayList<String> res = new ArrayList<>(count.folders);
        for (FolderInfo folderInfo : list.folders)
            res.add(folderInfo.getName());
        return res;
    }

    public void updateFolders(List<FolderInfo> folderInfos) {
        this.list.folders.clear();
        this.list.folders.addAll(folderInfos);
    }

    @Override
    public String toString() {
        return "{\ncount: {file: " + count.files + ", folder: " + count.folders + "}, \n" +
                "name: " + name + ", \n" +
                "size: " + size + ", \n" +
                "home: " + home + ", \n" +
                (list == null ? "}" : list + "}");
    }

    class EntriesCount {
        @SerializedName("folders")
        int folders;
        @SerializedName("files")
        int files;
    }
}