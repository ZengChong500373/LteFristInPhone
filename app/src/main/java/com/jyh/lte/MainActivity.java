package com.jyh.lte;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.jyh.lte.http.Network;
import com.jyh.lte.utils.BindLteListener;
import com.jyh.lte.utils.ChangeNet;
import com.jyh.lte.utils.CrashHandler;
import com.jyh.lte.utils.ToastUtils;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    private String tag = "jyh_MainActivity";
    IServiceOne iServiceOne;
    private TextView tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_content = (TextView) findViewById(R.id.tv_content);
        bindSer();
    }

    public void mainBind2lte(View view) {
        ChangeNet.requestMobileNet(MainActivity.this, new BindLteListener() {
            @Override
            public void BindForResult(final Boolean result) {
//                线程不一致
                Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        e.onNext(result);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean s) throws Exception {
                        tv_content.setText(s.toString());
                        if (s!=null&&s.toString().contains("true")){
                            Intent intent = new Intent();
                            intent.setAction("com.jyh.bindprocess");
                            sendBroadcast(intent);
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        tv_content.setText(throwable.toString());
                    }
                });
            }
        });
    }

    public void maingetData(View view) {
        Network.getNews().getDatafromNet().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                tv_content.setText(response.body().toString());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                tv_content.setText(t.toString());
            }
        });

    }

    public void bind2lte(View view) {
        try {
            iServiceOne.bind2Lte();
        } catch (Exception e) {
            CrashHandler.getInstance().handleException(e);
        }
    }

    public void getDataInService(View view) {
        try {
            iServiceOne.getData();
        } catch (Exception e) {
            ToastUtils.show("RemoteException_getData");
            e.printStackTrace();
            CrashHandler.getInstance().handleException(e);
        }

    }


    public void bindSer() {
        Intent intent = new Intent(MainActivity.this, ServiceOne.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iServiceOne = IServiceOne.Stub.asInterface(service);
            try {
                iServiceOne.registerListener(onDataListener);
            } catch (Exception e) {
                CrashHandler.getInstance().handleException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ServiceOneDataListener onDataListener = new ServiceOneDataListener.Stub() {
        @Override
        public void allDataArrive(String allData) throws RemoteException {
            Message msg = jyh.obtainMessage();
            Bundle b = new Bundle();
            b.putString("allData", allData);
            msg.setData(b);
            jyh.sendMessage(msg);
        }
    };
    Handler jyh = new Handler() {
        public void handleMessage(android.os.Message msg) {
            tv_content.setText(msg.getData().getString("allData"));
            Log.e("jyh", "allData" + msg.getData().getString("allData"));
        };
    };


}
