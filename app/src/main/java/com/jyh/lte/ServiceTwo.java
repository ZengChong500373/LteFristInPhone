package com.jyh.lte;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jyh.lte.utils.CustomNetUtils;
import com.jyh.lte.utils.MyLog;
import com.jyh.lte.utils.JyhConstant;

/**
 * Created by Administrator on 2017/6/4.
 */

public class ServiceTwo extends Service {
    private Bind2LTeInServiceTwoReceiver broadcast;

    @Override
    public void onCreate() {
        super.onCreate();
        initBrocast();
    }

    private void initBrocast() {
        IntentFilter intenFilter = new IntentFilter();
        intenFilter.addAction(JyhConstant.BIND_IN_PROCESS);
        intenFilter.addAction(JyhConstant.UNBIND_IN_PROCESS);
        broadcast = new Bind2LTeInServiceTwoReceiver();
        registerReceiver(broadcast, intenFilter);

    }

    public class Bind2LTeInServiceTwoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (JyhConstant.BIND_IN_PROCESS.equals(action)) {
                Boolean bindToMobileNet = CustomNetUtils.bindToMobileNet(JyhTapp.getContext());
                MyLog.write2File("bind Service two= " + bindToMobileNet);
                Log.i("jyh_two", "bindToMobileNet" + bindToMobileNet);
            } else if (JyhConstant.UNBIND_IN_PROCESS.equals(action)) {
                Boolean unBind = CustomNetUtils.cleanLte();
                MyLog.write2File("unbind Service two= " + unBind);
            }
        }
    }

    private IBinder serviceTwoBinder = new IServiceTwo.Stub() {
        @Override
        public void getData() throws RemoteException {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
