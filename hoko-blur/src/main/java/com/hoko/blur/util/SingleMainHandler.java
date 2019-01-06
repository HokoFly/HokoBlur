package com.hoko.blur.util;


import android.os.Handler;
import android.os.Looper;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public class SingleMainHandler {

    private static class MainHandlerHolder {
        private static Handler sMainHandler = new Handler(Looper.getMainLooper());
    }

    public static Handler get() {
        return MainHandlerHolder.sMainHandler;
    }
}
