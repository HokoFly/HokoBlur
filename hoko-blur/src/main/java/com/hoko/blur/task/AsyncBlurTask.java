package com.hoko.blur.task;

import android.graphics.Bitmap;

import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.api.IBlurResultDispatcher;

import static com.hoko.blur.task.AndroidBlurResultDispatcher.MAIN_THREAD_DISPATCHER;

/**
 * a wrapper class for async blur task
 * Created by yuxfzju on 2017/2/6.
 */

public abstract class AsyncBlurTask<T> implements Runnable {
    private Callback mCallback;

    IBlurProcessor mProcessor;

    private T mTarget;

    private IBlurResultDispatcher mResultDispatcher;

    public AsyncBlurTask(IBlurProcessor processor, T target, Callback callback) {
        mProcessor = processor;
        mTarget = target;
        mCallback = callback;
        mResultDispatcher = MAIN_THREAD_DISPATCHER;
    }

    @Override
    public void run() {
        /**
         * do blur
         */
        BlurResult result = new BlurResult(mCallback);
        try {
            if (mProcessor == null) {
                result.setSuccess(false);
                return;
            }

            result.setBitmap(makeBlur(mTarget));
            result.setSuccess(true);
        } catch (Throwable e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setError(e);
        } finally {
            mResultDispatcher.dispatch(result);
        }

    }

    protected abstract Bitmap makeBlur(T target);

    /**
     * set custom dispatcher to dispatch the result to other worker threads
     */
    public void setResultDispatcher(IBlurResultDispatcher resultDispatcher) {
        mResultDispatcher = resultDispatcher;
    }

    public interface Callback {
        void onBlurSuccess(Bitmap bitmap);

        void onBlurFailed(Throwable error);
    }
}