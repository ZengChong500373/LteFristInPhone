package com.jyh.lte;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonObject;
import com.jyh.lte.http.Network;
import com.jyh.lte.utils.ChangeNet;
import com.jyh.lte.utils.CrashHandler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Administrator on 2017/5/30.
 */

public class ServiceOne extends Service {
    private String tag = "jyh_ServiceOne";
    RemoteCallbackList<ServiceOneDataListener> mRemotelist = new RemoteCallbackList<ServiceOneDataListener>();
    private Bind2LTeInServiceBroadcastReceiver broadcast;

    @Override
    public void onCreate() {
        super.onCreate();
        initBrocast();
    }

    private void initBrocast() {
        IntentFilter intenFilter = new IntentFilter();
        intenFilter.addAction("com.jyh.bindprocess");
        broadcast = new Bind2LTeInServiceBroadcastReceiver();
        registerReceiver(broadcast, intenFilter);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceOneBinder;
    }

    private IBinder serviceOneBinder = new IServiceOne.Stub() {
        @Override
        public void getData() throws RemoteException {
            Log.i(tag, "getData");
            Network.getNews().getDatafromNet().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    dealData(response.body().toString());
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    dealData(t.toString());
                }
            });
        }

        @Override
        public void registerListener(ServiceOneDataListener listener) throws RemoteException {
            Log.i(tag, "registerListener");
            mRemotelist.register(listener);
            mRemotelist.beginBroadcast();
            mRemotelist.finishBroadcast();

        }

        @Override
        public void unRegisterListener() throws RemoteException {
            Log.i(tag, "unRegisterListener");
        }

        @Override
        public void bind2Lte() throws RemoteException {
            Log.i(tag, "bind2Lte");
            Boolean bin = ChangeNet.bindToMobileNet(JyhTapp.getContext());
            try {
                int num = mRemotelist.beginBroadcast();
                Log.e("jyh", "Handler num=" + num);
                for (int i = 0; i < num; ++i) {
                    ServiceOneDataListener listener = mRemotelist
                            .getBroadcastItem(i);
                    listener.allDataArrive("bind2Lte=" + bin);
                }
                mRemotelist.finishBroadcast();
            } catch (Exception e) {
                Log.e("jyh", "Exception=" + e.toString());
                CrashHandler.getInstance().handleException(e);
            }
        }
    };

    public void dealData(String str) {
        try {
            int num = mRemotelist.beginBroadcast();
            Log.e("jyh", "Handler num=" + num);
            for (int i = 0; i < num; ++i) {
                ServiceOneDataListener listener = mRemotelist
                        .getBroadcastItem(i);
                listener.allDataArrive(str);
            }
            mRemotelist.finishBroadcast();
        } catch (Exception e) {
            Log.e("jyh", "Exception=" + e.toString());
            CrashHandler.getInstance().handleException(e);
        }
    }

    public class Bind2LTeInServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.jyh.bindprocess".equals(action)) {
                Boolean bin = ChangeNet.bindToMobileNet(JyhTapp.getContext());
                try {
                    int num = mRemotelist.beginBroadcast();
                    Log.e("jyh", "Handler num=" + num);
                    for (int i = 0; i < num; ++i) {
                        ServiceOneDataListener listener = mRemotelist
                                .getBroadcastItem(i);
                        listener.allDataArrive("Bind2LTeInService=" + bin);
                    }
                    mRemotelist.finishBroadcast();
                } catch (Exception e) {
                    Log.e("jyh", "Exception=" + e.toString());
                    CrashHandler.getInstance().handleException(e);
                }
            }
        }
    }
}
