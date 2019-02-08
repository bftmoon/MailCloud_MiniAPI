package com.nodo.mailcloud_miniapi;

import com.google.gson.Gson;
import com.nodo.mailcloud_miniapi.json.AuthToken;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class Tokenizer {

    private final String login, password;
    private final OkHttpClient httpClient;
    private String token;

    Tokenizer(String login, String password, OkHttpClient httpClient) throws MailCloudException {
        this.login = login;
        this.password = password;
        this.httpClient = httpClient;
        recreateToken();
    }

    String getToken() {
        return token;
    }

    void recreateToken() throws MailCloudException {
        runRequest(RequestsUtil.login(this.login, this.password),
                "Login failed.")
                .close();

        if (httpClient.cookieJar().loadForRequest(RequestsUtil.getAuthUrl()).size() > 0) {
            runRequest(RequestsUtil.sdcCookie(),
                    "Sdc cookie request failed.")
                    .close();
            callToken();
        } else
            throw new MailCloudException(MailCloudException.Type.Unknown, "Token creation failed. Cookie wasn't response");
    }

    private void callToken() throws MailCloudException {
        Response response = runRequest(
                RequestsUtil.token(),
                "Token creation failed.");
        try {
            token = new Gson().fromJson(response.body().string(), AuthToken.class).getToken();
        } catch (IOException e) {
            throw new MailCloudException(MailCloudException.Type.ConnectionError, "Token creation failed. Null or empty token");
        }
        response.close();

        if (token == null || token.isEmpty())
            throw new MailCloudException(MailCloudException.Type.Unknown, "Token creation failed. Null or empty token");
    }

    private Response runRequest(Request request, String error_message) throws MailCloudException {
        Response response;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new MailCloudException(MailCloudException.Type.ConnectionError, "Response wasn't execute. " + e);
        }
        if (!response.isSuccessful()) {
            int code = response.code();
            response.close();
            MailCloudException.checkExceptionForCode(code, error_message);
        }
        return response;
    }
}
