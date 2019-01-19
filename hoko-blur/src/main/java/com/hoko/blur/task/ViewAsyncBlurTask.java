package com.hoko.blur.task;

import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.api.IBlurProcessor;

public class ViewAsyncBlurTask extends AsyncBlurTask<View> {
    public ViewAsyncBlurTask(IBlurProcessor processor, View target, Callback callback) {
        super(processor, target, callback);
    }

    @Override
    protected Bitmap makeBlur(View target) {
        return mProcessor.blur(target);
    }
}
