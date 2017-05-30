package com.jyh.lte.utils;



import android.widget.Toast;

import com.jyh.lte.JyhTapp;


public class ToastUtils {
	public static void show(String str) {
		Toast.makeText(JyhTapp.getContext(), str, 1).show();
	}
}
