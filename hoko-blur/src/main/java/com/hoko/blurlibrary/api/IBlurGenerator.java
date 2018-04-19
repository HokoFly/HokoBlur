package com.hoko.blurlibrary.api;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.task.AsyncBlurTask;

/**
 * Created by yuxfzju on 16/9/8.
 */
public interface IBlurGenerator extends IBlur {
    /**
     * 模糊操作
     *
     * @param inBitmap 输入的bitmap
     * @return
     */
    Bitmap blur(Bitmap inBitmap);

    void asyncBlur(Bitmap bitmap, AsyncBlurTask.CallBack callBack);


    void forceCopy(boolean isForceCopy);

    void needUpscale(boolean needUpscale);

}
