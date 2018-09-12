package com.hoko.blur.task;

import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.api.IBlurResultDispatcher;
import com.hoko.blur.util.SingleMainHandler;

/**
 * 异步模糊任务的封装
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

        mResultDelivery = new BlurResultDispatcher(SingleMainHandler.get());

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
            mResultDelivery.postResult(result);
        }

    }

    /**
     * 可自定义模糊结果的分发，如分发到其他worker thread
     * @param resultDelivery
     */
    public void setResultDelivery(IBlurResultDispatcher resultDelivery) {
        mResultDelivery = resultDelivery;
    }

    public interface Callback {
        void onBlurSuccess(Bitmap bitmap);

        void onBlurFailed(Throwable error);
    }
}