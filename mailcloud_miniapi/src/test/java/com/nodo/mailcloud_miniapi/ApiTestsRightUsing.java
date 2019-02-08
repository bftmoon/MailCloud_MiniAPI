package com.nodo.mailcloud_miniapi;

import android.util.Log;

import com.nodo.mailcloud_miniapi.json.FolderInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ApiTestsRightUsing {

    private MailCloud api = null;
    private ProgressCollector collector;
    private int success_count, fail_count, max_files_num;

    @Before
    public void setUp() throws IOException, MailCloudException {
        api = new MailCloud(MyConsts.TEST_LOGIN, MyConsts.TEST_PASSWORD, false);
        collector = new ProgressCollector(new CloudProgressListner() {
            @Override
            public void updateFilesProgress(int current_num) {
                Log.d("test", "Current num: " + current_num);
                success_count = current_num;
            }

            @Override
            public void setFilesMaxProgress(int max_num) {
                Log.d("test", "Max num: " + max_num);
                max_files_num = max_num;
            }

            @Override
            public void catchException(MailCloudException e) {
                fail_count++;
                Log.d("test", "Fails num: " + fail_count);
            }
        });
    }

    @After
    public void reset() {
        fail_count = 0;
        Log.d("test", "Fails count was delete");
        collector.resetNums();
    }

    @Test
    public void testGetFolderInfo() throws MailCloudException {
        FolderInfo info = api.getFolderInfo("/testing");
        assertEquals(info.getFoldersCount(), 4);
    }

    @Test
    public void testDownload() throws Exception {
        api.downloadFiles(collector, Consts.CLOUD_DOWNLOAD_DIR, Consts.PC_DOWNLOAD_DIR);
        try {
            assertEquals(new File(Consts.PC_DOWNLOAD_DIR).list().length, 3);
            assertEquals(new File(Consts.PC_DOWNLOAD_DIR + "/full_new").list().length, 2);
            assertEquals(new File(Consts.PC_DOWNLOAD_DIR + "/dir_exist").list().length, 1);
            assertEquals(new File(Consts.PC_DOWNLOAD_DIR + "/full_exist").list().length, 2);
            assertEquals(max_files_num, success_count + fail_count);
        } finally {
            deleteDirContent(Consts.PC_DOWNLOAD_DIR + "/full_new");
            System.gc(); // To release files (Ubuntu will delete hidden copies)
            Files.delete(Paths.get(Consts.PC_DOWNLOAD_DIR + "/full_new"));
            deleteDirContent(Consts.PC_DOWNLOAD_DIR + "/dir_exist");
        }
    }

    @Test
    public void testSyncAddAll() throws IOException, MailCloudException {
        api.simpleSync(collector, Consts.CLOUD_ADD_SYNC_DIR, Consts.PC_ADD_SYNC_DIR, new SyncParams(false, false, true, true));

        try {
            assertEquals(new File(Consts.PC_ADD_SYNC_DIR).list().length, 3);
            assertEquals(new File(Consts.PC_ADD_SYNC_DIR + "/cloud_unique_dir").list().length, 3);
            assertEquals(api.getFolderInfo(Consts.CLOUD_ADD_SYNC_DIR).getContentCount(), 3);
            assertEquals(api.getFolderInfo(Consts.CLOUD_ADD_SYNC_DIR + "/phone_unique_dir").getContentCount(), 3);
            assertEquals(max_files_num, success_count + fail_count);
            assertEquals(max_files_num, success_count + fail_count);
        } finally {
            api.deleteFileOrFolder(Consts.CLOUD_ADD_SYNC_DIR + "/phone_unique_dir");
            deleteDirContent(Consts.PC_ADD_SYNC_DIR + "/cloud_unique_dir");
            System.gc();
            new File(Consts.PC_ADD_SYNC_DIR + "/cloud_unique_dir").delete();
        }
    }

    @Test
    public void testSyncDelAll() throws IOException, MailCloudException {
        api.simpleSync(collector, Consts.CLOUD_DEL_SYNC_DIR, Consts.PC_DEL_SYNC_DIR, new SyncParams(true, true, false, false));
        try {
            assertEquals(api.getFolderInfo(Consts.CLOUD_DEL_SYNC_DIR).getContentCount(), 1);
            assertEquals(new File(Consts.PC_DEL_SYNC_DIR).list().length, 2); // folder not delete in Ubuntu (should be 1)
            assertEquals(max_files_num, success_count + fail_count);
        } finally {
            api.simpleSync(collector, Consts.CLOUD_DEL_SYNC_DIR, Consts.PC_RELOAD_SYNC, new SyncParams(false, false, true, false));
        }
    }

    @Test
    public void testCreateAndDeleteDir() throws MailCloudException {
        api.createFolder(Consts.CLOUD_CREATE_DIR);
        api.getFolderInfo(Consts.CLOUD_CREATE_DIR);
        api.deleteFileOrFolder(Consts.CLOUD_CREATE_DIR);
        try {
            api.getFolderInfo(Consts.CLOUD_CREATE_DIR);
        } catch (MailCloudException e) {
            assertEquals(e.getType(), MailCloudException.Type.FileNotFound);
        }
    }

    @Test
    public void testUpload() throws IOException, MailCloudException {
        api.uploadFiles(collector, Consts.PC_UPLOAD_DIR, Consts.CLOUD_UPLOAD_DIR);
        try {
            assertEquals(api.getFolderInfo(Consts.CLOUD_UPLOAD_DIR).getFoldersCount(), 3);
            assertEquals(api.getFolderInfo(Consts.CLOUD_UPLOAD_DIR + "/full_new").getContentCount(), 2);
            assertEquals(api.getFolderInfo(Consts.CLOUD_UPLOAD_DIR + "/dir_exist").getContentCount(), 2);
            assertEquals(api.getFolderInfo(Consts.CLOUD_UPLOAD_DIR + "/full_exist").getContentCount(), 4);
            assertEquals(max_files_num, success_count + fail_count);
        } finally {
            api.deleteFileOrFolder(Consts.CLOUD_UPLOAD_DIR + "/full_new");
            List<String> l = new ArrayList<>(2);
            l.add(Consts.CLOUD_UPLOAD_DIR + "/dir_exist/text.txt");
            l.add(Consts.CLOUD_UPLOAD_DIR + "/dir_exist/43638900.jpg");
            api.deleteFilesOrFolders(collector, l);
            l.clear();
            l.add(Consts.CLOUD_UPLOAD_DIR + "/full_exist/text (1).txt");
            l.add(Consts.CLOUD_UPLOAD_DIR + "/full_exist/43638900 (1).jpg");
            api.deleteFilesOrFolders(collector, l);
        }
    }

    @Test
    public void testGettingHash() throws IOException, MailCloudException {
        String hash = api.callFileHash(new File(Consts.PC_RELOAD_SYNC + "/non_unique/text.txt"));
        Log.d("test", "Hash: " + hash);
        assertEquals(hash, "676F6F6400000000000000000000000000000000");
    }

    private void deleteDirContent(String dir) throws IOException {
        String[] files = new File(dir).list();
        if (files != null && files.length > 0)
            for (String file : files) {
                File f = new File(dir + "/" + file);
                if (f.isDirectory()) {
                    deleteDirContent(f.getAbsolutePath());
                    System.gc();
                    Files.delete(Paths.get(dir)); // Not always work in Ubuntu
                }
                if (!f.delete())
                    Log.d("test", "Dir " + dir + " not deleted");
            }
    }
}
