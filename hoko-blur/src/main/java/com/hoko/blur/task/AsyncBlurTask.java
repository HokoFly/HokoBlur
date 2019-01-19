package com.hoko.blur.task;

import android.graphics.Bitmap;

import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.api.IBlurResultDispatcher;
import com.hoko.blur.util.Preconditions;

/**
 * a wrapper class for async blur task
 * Created by yuxfzju on 2017/2/6.
 */

public abstract class AsyncBlurTask<T> implements Runnable {
    private Callback mCallback;

    IBlurProcessor mProcessor;

    private T mTarget;

    private IBlurResultDispatcher mResultDispatcher;

    public AsyncBlurTask(IBlurProcessor processor, T target, Callback callback, IBlurResultDispatcher dispatcher) {
        mProcessor = processor;
        mTarget = target;
        mCallback = callback;
        mResultDispatcher = dispatcher;
    }

    @Override
    public void run() {
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
            Preconditions.checkNotNull(mResultDispatcher, "dispatcher == null");
            mResultDispatcher.dispatch(BlurResultRunnable.of(result));
        }

    }

    protected abstract Bitmap makeBlur(T target);

    public interface Callback {
        void onBlurSuccess(Bitmap bitmap);

        void onBlurFailed(Throwable error);
    }
}