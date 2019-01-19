package com.hoko.blur.task;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public class BlurResultRunnable implements Runnable {

    private BlurResult mResult;

    private BlurResultRunnable(BlurResult result) {
        mResult = result;
    }

    public static BlurResultRunnable of(BlurResult result) {
        return new BlurResultRunnable(result);
    }

    @Override
    public void run() {
        if (mResult != null) {
            if (mResult.isSuccess()) {
                if (mResult.getCallback() != null) {
                    mResult.getCallback().onBlurSuccess(mResult.getBitmap());
                }
            } else {
                if (mResult.getCallback() != null) {
                    mResult.getCallback().onBlurFailed(mResult.getError());
                }
            }
        }
    }
}
