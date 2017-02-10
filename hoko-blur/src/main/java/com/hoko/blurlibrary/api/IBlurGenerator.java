package com.hoko.blurlibrary.api;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.api.IBlur;
import com.hoko.blurlibrary.task.BlurTask;

/**
 * Created by xiangpi on 16/9/8.
 */
public interface IBlurGenerator extends IBlur {
    /**
     * 模糊操作
     *
     * @param inBitmap 输入的bitmap
     * @return
     */
    Bitmap doBlur(Bitmap inBitmap);

    void doAsyncBlur(Bitmap bitmap, BlurTask.CallBack callBack);


    void forceCopy(boolean isForceCopy);

    void needUpscale(boolean needUpscale);

}
