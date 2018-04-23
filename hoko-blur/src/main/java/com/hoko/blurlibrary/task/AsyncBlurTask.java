package com.hoko.blurlibrary.task;

import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blurlibrary.api.IBlurGenerator;
import com.hoko.blurlibrary.util.SingleMainHandler;

/**
 * 异步模糊任务的封装
 * Created by yuxfzju on 2017/2/6.
 */

public class AsyncBlurTask implements Runnable {
    private Callback mCallback;

    private IBlurGenerator mGenerator;

    private Bitmap mBitmap;

    private View mView;

    private BlurResultDelivery mResultDelivery;

    public AsyncBlurTask(IBlurGenerator generator, Bitmap bitmap, Callback callback) {
        mGenerator = generator;
        mBitmap = bitmap;
        mCallback = callback;

        mResultDelivery = new BlurResultDelivery(SingleMainHandler.get());

    }

    public AsyncBlurTask(IBlurGenerator generator, View view, Callback callback) {
        this(generator, (Bitmap)null, callback);
        mView = view;
    }

    @Override
    public void run() {
        /**
         * do blur
         */
        BlurResult result = new BlurResult(mCallback);
        try {
            if (mGenerator == null) {
                result.setSuccess(false);
                return;
            }

            if (mView != null) {
                result.setBitmap(mGenerator.blur(mView));
            } else {
                result.setBitmap(mGenerator.blur(mBitmap));
            }

            result.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
            mResultDelivery.postResult(result);
        }

    }

    /**
     * 可自定义模糊结果的分发，如分发到其他worker thread
     * @param resultDelivery
     */
    public void setResultDelivery(BlurResultDelivery resultDelivery) {
        mResultDelivery = resultDelivery;
    }

    public interface Callback {
        void onBlurSuccess(Bitmap bitmap);

        void onBlurFailed();
    }
}