package com.jyh.lte;



import android.app.Application;
import android.content.Context;

import com.jyh.lte.utils.CrashHandler;
public class JyhTapp extends Application {
	private static Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		this.mContext = getApplicationContext();
		CrashHandler.getInstance().init(mContext);
	}

	public static Context getContext() {
		return mContext;
	}
}
