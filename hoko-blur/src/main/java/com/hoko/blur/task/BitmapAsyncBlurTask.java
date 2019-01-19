package com.hoko.blur.task;

import android.graphics.Bitmap;

import com.hoko.blur.api.IBlurProcessor;

public class BitmapAsyncBlurTask extends AsyncBlurTask<Bitmap> {
    public BitmapAsyncBlurTask(IBlurProcessor processor, Bitmap bitmap, Callback callback) {
        super(processor, bitmap, callback);
    }

    @Override
    protected Bitmap makeBlur(Bitmap target) {
        return mProcessor.blur(target);
    }
}
