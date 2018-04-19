package com.hoko.blurlibrary.task;

import android.graphics.Bitmap;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public class BlurResult {
    private boolean isSuccess;

    private Bitmap bitmap;

    private AsyncBlurTask.CallBack callBack;

    public BlurResult(AsyncBlurTask.CallBack callBack) {
        this.callBack = callBack;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public AsyncBlurTask.CallBack getCallBack() {
        return callBack;
    }
}
