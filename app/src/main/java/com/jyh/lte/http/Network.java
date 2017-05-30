package com.jyh.lte.http;

import com.jyh.lte.JyhTapp;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 2016/9/20 0020.
 */
public class Network {
    private static OkHttpClient mOkHttpClient;
    private static Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
     private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJava2CallAdapterFactory.create();
    private static NewsMethods newsMethods;


    private static void initOkhttp() {
        if (mOkHttpClient == null) {
            synchronized (Network.class) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = new OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
    }

    public static NewsMethods getNews() {
        initOkhttp();
        if (newsMethods == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(mOkHttpClient)
                    .baseUrl(GlobalUrl.NEWS_BASE)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            newsMethods = retrofit.create(NewsMethods.class);
        }
        return newsMethods;
    }


}
