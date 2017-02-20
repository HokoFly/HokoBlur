package com.hoko.blurlibrary.task;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.generator.BlurGenerator;
import com.hoko.blurlibrary.util.SingleMainHandler;

/**
 * 异步模糊任务的封装
 * Created by xiangpi on 2017/2/6.
 */

public class AsyncBlurTask implements Runnable {
    private CallBack mCallBack;

    private BlurGenerator mGenerator;

    private Bitmap mBitmap;

    private BlurResultDelivery mResultDelivery;

    public AsyncBlurTask(BlurGenerator generator, Bitmap bitmap, CallBack callBack) {
        mGenerator = generator;
        mBitmap = bitmap;
        mCallBack = callBack;

        mResultDelivery = new BlurResultDelivery(SingleMainHandler.get());

    }

    @Override
    public void run() {
        /**
         * do blur
         */
        BlurResult result = new BlurResult(mCallBack);
        try {
            if (mGenerator == null) {
                result.setSuccess(false);
                return;
            }

            result.setBitmap(mGenerator.doBlur(mBitmap, false));
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

    public interface CallBack {
        void onBlurSuccess(Bitmap bitmap);

        void onBlurFailed();
    }
}