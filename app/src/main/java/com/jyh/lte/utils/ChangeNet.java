package com.jyh.lte.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.ConnectivityManager.NetworkCallback;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.jyh.lte.JyhTapp;

public class ChangeNet {

    //	public static final String TAG = "@xz++@";
    public static final String TAG = "jyh_ChangeNet";
    private static NetworkCallback mCallback = null;
    private static boolean mBindResult = false;

    @SuppressLint("NewApi")
    public static void requestMobileNet(final Context context, final BindLteListener listener) {
        if (mCallback != null) {
            Log.e("jyh", "mCallback  is not null!");
            return;
        } else {
            Log.e("jyh", "mCallback  is  null!");
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
                Log.e("jyh", "requestMobileNet  success!!");
                ConnectivityManager conMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBindResult = conMgr.bindProcessToNetwork(network);
                } else {
                    mBindResult = conMgr.setProcessDefaultNetwork(network);
                }
                listener.BindForResult(mBindResult);
                Log.e("jyh", "bindMobileNet result = " + mBindResult);
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
//                listener.BindForResult(mBindResult);
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);

            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);

            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
               /* 移除自建网络*/
                cleanLte();
            }

        };
        conMgr.requestNetwork(request, mCallback);
    }



    @SuppressLint("NewApi")
    public static boolean bindToMobileNet(Context context) {
        boolean bindResult = false;
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network boundNet = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boundNet = conMgr.getBoundNetworkForProcess();
        } else {
            boundNet = conMgr.getProcessDefaultNetwork();
        }

        int boundNetType = -1;
        NetworkInfo boundNetInfo = null;
        if (boundNet != null) {
            boundNetInfo = conMgr.getNetworkInfo(boundNet);
            if (boundNetInfo != null) {
                boundNetType = boundNetInfo.getType();
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
            if (boundNetType != ConnectivityManager.TYPE_MOBILE && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bindResult = conMgr.bindProcessToNetwork(network);
                } else {
                    bindResult = conMgr.setProcessDefaultNetwork(network);
                }
                break;
            }
        }
        return bindResult;
    }

    public static void cleanLte(){
        ConnectivityManager conMgr = (ConnectivityManager) JyhTapp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
