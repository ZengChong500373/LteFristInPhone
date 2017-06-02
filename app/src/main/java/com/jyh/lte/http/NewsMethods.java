package com.jyh.lte.http;



import com.google.gson.JsonObject;

import org.json.JSONObject;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * Created by Administrator on 2016/9/20 0020.
 *public  static String NEWS_BASE="http://c.m.163.com/nc/article/";
 */
public interface NewsMethods {

    @GET("headline/T1348647909107/0-20.html")
    Call<JsonObject> getDatafromNet();

}
