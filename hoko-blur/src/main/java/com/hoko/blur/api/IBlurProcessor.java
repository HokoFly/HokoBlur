package com.hoko.blur.api;

import android.graphics.Bitmap;
import android.view.View;

import com.hoko.blur.anno.Scheme;
import com.hoko.blur.task.AsyncBlurTask;

/**
 * Created by yuxfzju on 16/9/8.
 */
public interface IBlurProcessor extends IParams, ITranslate {
    /**
     * 模糊操作
     *
     * @param bitmap 输入的bitmap
     * @return
     */
    Bitmap blur(Bitmap bitmap);

    Bitmap blur(View view);

    void asyncBlur(Bitmap bitmap, AsyncBlurTask.Callback callback);

    void asyncBlur(View view, AsyncBlurTask.Callback callback);

    @Scheme
    int scheme();

    boolean forceCopy();

    boolean needUpscale();

}
