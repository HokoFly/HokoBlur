package com.hoko.blurlibrary.util;


import android.os.Handler;
import android.os.Looper;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public class SingleMainHandler {
    private static volatile Handler sMainHandler;

    public static Handler get() {
        if (sMainHandler == null) {
            synchronized (SingleMainHandler.class) {
                if (sMainHandler == null) {
                    sMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return sMainHandler;
    }
}
