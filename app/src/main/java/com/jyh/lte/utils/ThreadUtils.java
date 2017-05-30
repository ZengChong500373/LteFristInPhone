package com.jyh.lte.utils;

import android.os.Looper;

public class ThreadUtils {
	public static boolean isMainThread() {
	    return Looper.getMainLooper().getThread() == Thread.currentThread();
	}
}
