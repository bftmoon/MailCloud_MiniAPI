package com.nodo.mailcloud_miniapi;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

class RequestsUtil {

    static final String baseUrl = "https://cloud.mail.ru/api/v2/";
    private static final String authUrl = "https://auth.mail.ru/cgi-bin/auth";
    private static final String sdcUrl = "https://auth.mail.ru/sdc?from=https://cloud.mail.ru/home";
    private static final String csrfUrl = "https://cloud.mail.ru/api/v2/tokens/csrf";

    static HttpUrl getAuthUrl() {
        return HttpUrl.parse(authUrl);
    }

    static Request login(String login, String password) {
        return new Request.Builder()
                .url(authUrl)
                .post(new FormBody.Builder()
                        .addEncoded("Login", login)
                        .addEncoded("Password", password)
                        .build())
                .build();
    }

    static Request sdcCookie() {
        return new Request.Builder().url(sdcUrl).build();
    }

    static Request token() {
        return new Request.Builder().url(csrfUrl).build();
    }
}
