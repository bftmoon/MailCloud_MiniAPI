package com.nodo.mailcloud_miniapi;

import com.google.gson.JsonObject;
import com.nodo.mailcloud_miniapi.json.Dispatche;
import com.nodo.mailcloud_miniapi.json.InfoWithBody;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

interface RetroCloudApi {

    @Headers("Accept: application/json")
    @GET("folder")
    Call<InfoWithBody> getFolderInfo(
            @Query("home") String fullpath,
            @Query("token") String token);

    @Headers("Accept: application/json")
    @GET("file")
    Call<InfoWithBody> getFileInfo(
            @Query("home") String fullpath,
            @Query("token") String token);

    @Headers("Accept: application/json")
    @POST("file/remove")
    Call<JsonObject> deleteFileOrFolder(
            @Query("home") String fullpath,
            @Query("token") String token);

    @Headers("Accept: application/json")
    @POST("folder/add")
    Call<JsonObject> addFolder(
            @Query("home") String fullpath,
            @Query("token") String token);

    @Headers("Accept: application/json")
    @GET("dispatcher")
    Call<Dispatche> getDispatcherUrls(
            @Query("token") String token);

    @GET
    Call<ResponseBody> download(@Url String url);

    @Multipart
    @POST
    Call<ResponseBody> startUpload(@Url String url,
                                   @Part MultipartBody.Part file);

    @POST("file/add")
    Call<ResponseBody> endUpload(
            @Query("home") String cloud_filepath,
            @Query("hash") String hash,
            @Query("conflict") String conflict,
            @Query("token") String token,
            @Query("size") long size

    );
}


