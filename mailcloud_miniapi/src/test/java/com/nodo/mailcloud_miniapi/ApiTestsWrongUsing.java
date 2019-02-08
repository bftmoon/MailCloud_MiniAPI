package com.nodo.mailcloud_miniapi;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ApiTestsWrongUsing {
    MailCloudException exception;
    private MailCloud api = null;
    private ProgressCollector collector;
    private int current_file;
    private int max_files_num;

    @Before
    public void setUp() throws IOException, MailCloudException {
        api = new MailCloud(MyConsts.TEST_LOGIN, MyConsts.TEST_PASSWORD, false);
        collector = new ProgressCollector(new CloudProgressListner() {
            @Override
            public void updateFilesProgress(int current_num) {
                current_file++;
                if (current_file == max_files_num)
                    Log.v("Collector", "Last file loaded");
            }

            @Override
            public void setFilesMaxProgress(int max_num) {
                max_files_num = max_num;
            }

            @Override
            public void catchException(MailCloudException e) {
                Log.e("Collector", e.getMessage());
                exception = e;
            }
        });

        current_file = 0;
    }

    @Test
    public void authWrongData() {
        try {
            new MailCloud("jkhi", "hjbkhb", false);
        } catch (IOException e) {
            Log.e("Test", "Wrong result");
        } catch (MailCloudException e) {
            assertEquals(e.getType(), MailCloudException.Type.AccessDenied);
        }
    }

    @Test
    public void testGetFailFolderInfo() {
        try {
            api.getFolderInfo("gfxhgc");
        } catch (MailCloudException e) {
            assertEquals(e.getType(), MailCloudException.Type.FileNotFound);
        }
    }

    @Test
    public void testDownloadWrongPath() {
        api.downloadFile(collector, "jhgbkjn", "hghkhilj");
        assertEquals(exception.getType(), MailCloudException.Type.FileNotFound);
    }

    @Test
    public void anyWithNull() throws IOException, MailCloudException {
        try {
            api.callFileHash(null);
        } catch (IllegalArgumentException e) {
            Log.d("test", "Null shall not pass");
        }
    }
}
