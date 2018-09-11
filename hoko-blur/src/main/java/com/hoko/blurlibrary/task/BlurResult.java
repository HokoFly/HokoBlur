package com.hoko.blurlibrary.task;

import android.graphics.Bitmap;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public class BlurResult {
    private boolean isSuccess;

    private Bitmap bitmap;

    private AsyncBlurTask.Callback callback;

    private Throwable e;

    public BlurResult(AsyncBlurTask.Callback callback) {
        this.callback = callback;
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

    public AsyncBlurTask.Callback getCallback() {
        return callback;
    }

    public Throwable getError() {
        return e;
    }

    public void setError(Throwable e) {
        this.e = e;
    }
}
