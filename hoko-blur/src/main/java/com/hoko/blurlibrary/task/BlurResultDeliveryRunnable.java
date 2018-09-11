package com.hoko.blurlibrary.task;

/**
 * Created by yuxfzju on 2017/2/7.
 */

class BlurResultDeliveryRunnable implements Runnable {

    private BlurResult mResult;

    BlurResultDeliveryRunnable(BlurResult result) {
        mResult = result;
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
