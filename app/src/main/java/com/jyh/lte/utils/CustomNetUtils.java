package com.jyh.lte.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.ConnectivityManager.NetworkCallback;
import android.os.Build;
import android.util.Log;

import com.jyh.lte.JyhTapp;

/**
 * 自定义使用网络的工具类
 */
public class CustomNetUtils {


    public static final String TAG = "jyh_CustomNetUtils";
    private static NetworkCallback mCallback = null;
    private static boolean mBindResult = false;

    @SuppressLint("NewApi")
    public static void requestMobileNet(final Context context, final BindLteListener listener) {
        if (mCallback != null) {
            Log.e("jyh", "mCallback  is not null!");
//            cleanLteInMainProcess();
            mCallback=null;
        }
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest request = builder.build();
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mCallback = new NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.e("jyh_NetworkCallback", "requestMobileNet  success!!");
                ConnectivityManager conMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = conMgr.getNetworkInfo(network);
                if (networkInfo==null){
                    return;
                }else {
                  int type=  networkInfo.getType();
                    Log.e("jyh_NetworkCallback_av", "type "+ type+networkInfo.getSubtypeName());
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBindResult = conMgr.bindProcessToNetwork(network);
                    Log.e("jyh_NetworkCallback_av", ">23 "+ mBindResult);
                } else {
                    mBindResult = conMgr.setProcessDefaultNetwork(network);
                    Log.e("jyh_NetworkCallback_av", "<23 "+ mBindResult);
                }
                listener.BindForResult("onAvailable", mBindResult);
                Log.e("jyh_NetworkCallback_av", "bindMobileNet result = " + mBindResult);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                ConnectivityManager conMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBindResult = conMgr.bindProcessToNetwork(network);
                } else {
                    mBindResult = conMgr.setProcessDefaultNetwork(network);
                }
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                listener.BindForResult("onLinkPropertiesChanged", mBindResult);
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                listener.BindForResult("onLosing", mBindResult);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
               /* 移除自建网络*/
                cleanLteInMainProcess();
                listener.BindForResult("onLost", mBindResult);
            }

        };
        conMgr.requestNetwork(request, mCallback);
    }


    @SuppressLint("NewApi")
    public static boolean bindToMobileNet(Context context) {
        boolean bindResult = false;
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network boundNetInProcess = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boundNetInProcess = conMgr.getBoundNetworkForProcess();
        } else {
            boundNetInProcess = conMgr.getProcessDefaultNetwork();
        }

        int boundNetInProcessType = -1;
        NetworkInfo boundNetInfo = null;
        if (boundNetInProcess != null) {
            boundNetInfo = conMgr.getNetworkInfo(boundNetInProcess);
            if (boundNetInfo != null) {
                boundNetInProcessType = boundNetInfo.getType();
            }
        }
        Network[] networks = conMgr.getAllNetworks();
        if (networks == null) {
            return false;
        }
        Log.d(TAG, "networks.length is " + networks.length);
        for (Network network : networks) {
            NetworkInfo info = conMgr.getNetworkInfo(network);
            Log.d(TAG, "NetworkType is " + info.getTypeName());
            if (boundNetInProcessType != ConnectivityManager.TYPE_MOBILE && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bindResult = conMgr.bindProcessToNetwork(network);
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    bindResult = conMgr.setProcessDefaultNetwork(network);
                }
                break;
            }
        }
        return bindResult;
    }

    @SuppressLint("NewApi")
    public static void cleanLteInMainProcess() {
        ConnectivityManager conMgr = (ConnectivityManager) JyhTapp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cleanLte(conMgr);
        if (mCallback==null){
            return;
        }
        conMgr.unregisterNetworkCallback(mCallback);
        mCallback = null;
        Intent intent = new Intent();
        intent.setAction(JyhConstant.UNBIND_IN_PROCESS);
        JyhTapp.getContext().sendBroadcast(intent);
    }

    public static Boolean cleanLte(ConnectivityManager conMgr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return conMgr.bindProcessToNetwork(null);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return conMgr.setProcessDefaultNetwork(null);
        }
        return false;
    }
    public static Boolean cleanLte() {
        ConnectivityManager conMgr = (ConnectivityManager) JyhTapp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return conMgr.bindProcessToNetwork(null);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return conMgr.setProcessDefaultNetwork(null);
        }
        return false;
    }
}
