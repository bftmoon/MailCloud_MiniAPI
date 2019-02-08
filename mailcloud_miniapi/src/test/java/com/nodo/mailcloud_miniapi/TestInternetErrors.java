package com.nodo.mailcloud_miniapi;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestInternetErrors {

    private MailCloud api = null;

    @Before
    public void setUp() throws IOException, MailCloudException {
        api = new MailCloud(MyConsts.TEST_LOGIN, MyConsts.TEST_PASSWORD, false);
    }

    @Test
    public void anyWithoutInternet() throws IOException, InterruptedException {
        Runtime.getRuntime().exec(Consts.NETWORK_OFF_COMMAND);
        Thread.sleep(150);
        try {
            api.getFolderInfo("fgdff");
        } catch (MailCloudException e) {
            assertEquals(e.getType(), MailCloudException.Type.ConnectionError);
        }
    }

    @After
    public void onWifi() throws IOException {
        Runtime.getRuntime().exec(Consts.NETWORK_ON_COMMAND);
    }
}
