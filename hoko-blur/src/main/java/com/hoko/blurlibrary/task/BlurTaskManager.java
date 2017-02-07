package com.hoko.blurlibrary.task;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xiangpi on 2017/2/7.
 */

public class BlurTaskManager {
    // 线程数到可用cpu核数的一半
    private static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() <= 3 ?
            1 : Runtime.getRuntime().availableProcessors() / 2;

    private static final ExecutorService BLUR_EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    private static volatile BlurTaskManager sInstance;

    private BlurTaskManager() {
    }

    public static BlurTaskManager getInstance() {
        if (sInstance == null) {
            synchronized (BlurTaskManager.class) {
                if (sInstance == null) {
                    sInstance = new BlurTaskManager();
                }
            }
        }

        return sInstance;
    }

    public void submit(BlurTask task) {
        if (task != null) {
            BLUR_EXECUTOR.submit(task);
        }

    }
}
