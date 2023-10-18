package com.hoko.blur.task;

import android.graphics.Bitmap;

/**
 * Created by yuxfzju on 2017/2/7.
 */

class BlurResult {
    private boolean success;

    private Bitmap bitmap;

    private final AsyncBlurTask.Callback callback;

    private Throwable e;

    public BlurResult(AsyncBlurTask.Callback callback) {
        this.callback = callback;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
