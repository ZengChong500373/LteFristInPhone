package com.jyh.lte;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.net.ConnectivityManager;
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
import com.jyh.lte.utils.CustomNetUtils;
import com.jyh.lte.utils.CrashHandler;
import com.jyh.lte.utils.JyhConstant;
import com.jyh.lte.utils.ToastUtils;

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
    private IServiceOne iServiceOneInMain;
    private TextView tv_content;

    private NetChangBrocast brocast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_content = (TextView) findViewById(R.id.tv_content);
        bindSer();
        initBrocast();
    }

    public void initBrocast() {
        IntentFilter intenFilter = new IntentFilter();
        intenFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intenFilter.addAction(JyhConstant.UNBIND_IN_PROCESS);
        intenFilter.addAction(JyhConstant.REBIND_IN_PROCESS);

        brocast = new NetChangBrocast();
        registerReceiver(brocast, intenFilter);
    }

    public void mainBind2lte(View view) {
        useLte();
    }

    public void maingetData(View view) {
        Network.getNews().getDatafromNet().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response == null) return;
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
            iServiceOneInMain.bind2Lte();
        } catch (Exception e) {
            CrashHandler.getInstance().handleException(e);
        }
    }

    public void getDataInServiceOne(View view) {
        try {
            iServiceOneInMain.getData();
        } catch (Exception e) {
            ToastUtils.show("RemoteException_getDataInServiceOne");
            e.printStackTrace();
            CrashHandler.getInstance().handleException(e);
        }

    }

    public void getDataInServiceTwo(View view) {
        try {

        } catch (Exception e) {
            ToastUtils.show("RemoteException_getDataInServiceTwo");
            e.printStackTrace();
            CrashHandler.getInstance().handleException(e);
        }
    }

    public void cleanTv(View view) {
        tv_content.setText("");
    }

    public void clreanInmain(View view) {
        CustomNetUtils.cleanLteInMainProcess();
    }

    public void bindSer() {
        Intent intent1 = new Intent(MainActivity.this, ServiceOne.class);
        bindService(intent1, serviceConnection, Context.BIND_AUTO_CREATE);
        Intent intent2 = new Intent(MainActivity.this, ServiceTwo.class);
        startService(intent2);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IServiceOne iServiceOne = IServiceOne.Stub.asInterface(service);
            try {
                if (iServiceOne != null) {
                    iServiceOneInMain = iServiceOne;
                    iServiceOneInMain.registerListener(onDataListener);
                }
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
            Log.e("jyh_handler", "allData" + msg.getData().getString("allData"));
        }

        ;

    };

    public class NetChangBrocast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ToastUtils.show(action);
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                tv_content.setText(action);
                Log.e("jyh", "NetChangBrocast");
            } else if (JyhConstant.UNBIND_IN_PROCESS.equals(action) || JyhConstant.REBIND_IN_PROCESS.equals(action)) {
                Log.i("jyh_NetChangBrocast", "1");
                tv_content.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("jyh_NetChangBrocast", "2");
                        useLte();
                    }
                }, 200);
            }

        }
    }

    public void useLte() {
        CustomNetUtils.requestMobileNet(MainActivity.this, new BindLteListener() {
            @Override
            public void BindForResult(final String methodname, final Boolean result) {
                {
//                线程不一致
                    Observable.create(new ObservableOnSubscribe<Boolean>() {
                        @Override
                        public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                            e.onNext(result);
                        }
                    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean s) throws Exception {
                            tv_content.setText(methodname + " " + s.toString());
                            if (result&&"onAvailable".equals(methodname)) {
                                Intent intent = new Intent();
                                intent.setAction(JyhConstant.BIND_IN_PROCESS);
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
            }
        });
    }
}
