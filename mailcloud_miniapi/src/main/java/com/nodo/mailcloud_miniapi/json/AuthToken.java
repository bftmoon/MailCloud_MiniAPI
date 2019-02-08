package com.nodo.mailcloud_miniapi.json;

import com.google.gson.annotations.SerializedName;

public class AuthToken extends JsonResponse<AuthToken.AuthTokenBody> {

    public String getToken() {
        return body().token;
    }

    class AuthTokenBody {
        @SerializedName("token")
        String token;
    }
}
