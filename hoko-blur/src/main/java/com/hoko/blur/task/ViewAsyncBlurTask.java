package com.hoko.blur.task;

import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.api.IBlurProcessor;
import com.hoko.blur.api.IBlurResultDispatcher;

public class ViewAsyncBlurTask extends AsyncBlurTask<View> {
    public ViewAsyncBlurTask(IBlurProcessor processor, View target, Callback callback, IBlurResultDispatcher dispatcher) {
        super(processor, target, callback, dispatcher);
    }

    @Override
    protected Bitmap makeBlur(View target) {
        return mProcessor.blur(target);
    }
}
