package com.hoko.blur.task;

import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.api.IBlurResultDispatcher;
import com.hoko.blur.util.SingleMainHandler;

import static com.hoko.blur.task.AndroidBlurResultDispatcher.MAIN_THREAD_DISPATCHER;

/**
 * a wrapper class for async blur task
 * Created by yuxfzju on 2017/2/6.
 */

public class AsyncBlurTask implements Runnable {
    private Callback mCallback;

    private IBlurProcessor mProcessor;

    private Bitmap mBitmap;

    private View mView;

    private IBlurResultDispatcher mResultDelivery;

    public AsyncBlurTask(IBlurProcessor processor, Bitmap bitmap, Callback callback) {
        mProcessor = processor;
        mBitmap = bitmap;
        mCallback = callback;
        mResultDelivery = MAIN_THREAD_DISPATCHER;
    }

    public AsyncBlurTask(IBlurProcessor processor, View view, Callback callback) {
        this(processor, (Bitmap)null, callback);
        mView = view;
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

            if (mView != null) {
                result.setBitmap(mProcessor.blur(mView));
            } else {
                result.setBitmap(mProcessor.blur(mBitmap));
            }

            result.setSuccess(true);
        } catch (Throwable e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setError(e);
        } finally {
            mResultDelivery.dispatch(result);
        }

    }

    /**
     * set custom dispatcher to dispatch the result to other worker threads
     */
    public void setResultDelivery(IBlurResultDispatcher resultDelivery) {
        mResultDelivery = resultDelivery;
    }

    public interface Callback {
        void onBlurSuccess(Bitmap bitmap);

        void onBlurFailed(Throwable error);
    }
}