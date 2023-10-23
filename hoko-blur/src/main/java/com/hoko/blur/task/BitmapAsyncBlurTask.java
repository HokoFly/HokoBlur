package com.hoko.blur.task;

import android.graphics.Bitmap;

import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.api.IBlurResultDispatcher;

public final class BitmapAsyncBlurTask extends AsyncBlurTask<Bitmap> {
    public BitmapAsyncBlurTask(IBlurProcessor processor, Bitmap bitmap, Callback callback, IBlurResultDispatcher dispatcher) {
        super(processor, bitmap, callback, dispatcher);
    }

    @Override
    protected Bitmap makeBlur(Bitmap target) {
        return mProcessor.blur(target);
    }
}
