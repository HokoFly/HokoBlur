package com.hoko.blur.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.anno.Mode;
import com.hoko.blur.anno.Scheme;
import com.hoko.blur.processor.BlurProcessor;
import com.hoko.blur.task.AsyncBlurTask;

import java.util.concurrent.Future;

public interface IBlurBuild {
    IBlurBuild context(Context context);

    IBlurBuild mode(@Mode int mode);

    IBlurBuild scheme(@Scheme int scheme);

    IBlurBuild radius(int radius);

    IBlurBuild sampleFactor(float sampleFactor);

    IBlurBuild forceCopy(boolean isForceCopy);

    IBlurBuild needUpscale(boolean needUpscale);

    IBlurBuild translateX(int translateX);

    IBlurBuild translateY(int translateY);

    IBlurBuild dispatcher(IBlurResultDispatcher dispatcher);

    /**
     * Get different types of Blur Processors
     */
    BlurProcessor processor();

    Bitmap blur(Bitmap bitmap);

    Bitmap blur(View view);

    Future asyncBlur(Bitmap bitmap, AsyncBlurTask.Callback callback);

    Future asyncBlur(View view, AsyncBlurTask.Callback callback);

}
