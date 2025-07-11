package com.hoko.blur.task;

import android.util.Log;

import com.hoko.blur.util.Preconditions;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public final class BlurTaskManager {
    private static final String TAG = BlurTaskManager.class.getSimpleName();

    //Threads count is a half of cpu cores
    private static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() <= 3 ?
            1 : Runtime.getRuntime().availableProcessors() / 2;

    private static final ExecutorService ASYNC_BLUR_EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    private static final ExecutorService CONCURRENT_BLUR_EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    private static final ExecutorService TASK_QUEUE_EXECUTOR = Executors.newSingleThreadExecutor();

    private static class BlurTaskManagerHolder {
        private static final BlurTaskManager INSTANCE = new BlurTaskManager();
    }

    private BlurTaskManager() {
    }

    public static BlurTaskManager getInstance() {
       return BlurTaskManagerHolder.INSTANCE;
    }

    public Future<?> submit(AsyncBlurTask<?> task) {
        Preconditions.checkNotNull(task, "task == null");
        return ASYNC_BLUR_EXECUTOR.submit(task);
    }

    public void invokeAll(Collection<BlurSubTask> tasks) {
        Preconditions.checkNotNull(tasks, "tasks == null");
        if (tasks.size() > 0) {
            try {
                CONCURRENT_BLUR_EXECUTOR.invokeAll(tasks);
            } catch (InterruptedException e) {
                Log.e(TAG, "invoke blur sub tasks error", e);
            }
        }
    }

    public Future<?> enqueue(AsyncBlurTask<?> task) {
        Preconditions.checkNotNull(task, "task == null");
        return TASK_QUEUE_EXECUTOR.submit(task);
    }

    public static int getWorkersNum() {
        return EXECUTOR_THREADS;
    }
}
