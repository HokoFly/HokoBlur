package com.hoko.blurlibrary.task;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.api.IBlurGenerator;
import com.hoko.blurlibrary.util.SingleMainHandler;

/**
 * Created by xiangpi on 2017/2/6.
 */

public class BlurTask implements Runnable {
    private CallBack mCallBack;

    private IBlurGenerator mGenerator;

    private Bitmap mBitmap;

    private BlurResultDelivery mResultDelivery;

    public BlurTask(IBlurGenerator generator, Bitmap bitmap, CallBack callBack) {
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

            result.setBitmap(mGenerator.doBlur(mBitmap));
            result.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
            mResultDelivery.postResult(result);
        }

    }

    public void setmResultDelivery(BlurResultDelivery resultDelivery) {
        mResultDelivery = resultDelivery;
    }

    public interface CallBack {
        void onBlurSuccess(Bitmap bitmap);

        void onBlurFailed();
    }
}
