package com.hoko.blurlibrary.task;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public class BlurTaskManager {
    // 线程数到可用cpu核数的一半
    private static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() <= 3 ?
            1 : Runtime.getRuntime().availableProcessors() / 2;
    //异步任务线程池
    private static final ExecutorService ASYNC_BLUR_EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    //每一次模糊采用线程池进行并发处理，将bitmap分块模糊
    private static final ExecutorService CONCURRENT_BLUR_EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

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

    public void submit(AsyncBlurTask task) {
        if (task != null) {
            ASYNC_BLUR_EXECUTOR.submit(task);
        }
    }

    public void invokeAll(Collection<BlurSubTask> tasks) {
        if (tasks != null && tasks.size() > 0) {
            try {
                CONCURRENT_BLUR_EXECUTOR.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getCores() {
        return EXECUTOR_THREADS;
    }
}
