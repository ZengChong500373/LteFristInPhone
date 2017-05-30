package com.jyh.lte.utils;


import com.google.gson.JsonObject;
import com.jyh.lte.http.Network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JyhHttp {
    public static String url = "http://c.m.163.com/nc/article/headline/T1348647909107/0-20.html";

    public static void getAllData() {
        Network.getNews().getDatafromNet().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

}
