package com.nodo.mailcloud_miniapi.json;

import java.util.ArrayList;
import java.util.List;

public class EntriesLists {

    final List<FolderInfo> folders;
    final List<FileInfo> files;

    EntriesLists() {
        folders = new ArrayList<>();
        files = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "{\nfolders: " + folders + ", \nfiles: " + files + "}";
    }
}
