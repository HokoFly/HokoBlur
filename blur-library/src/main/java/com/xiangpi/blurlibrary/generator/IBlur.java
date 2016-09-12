package com.xiangpi.blurlibrary.generator;

import android.graphics.Bitmap;

import com.xiangpi.blurlibrary.Blur;

/**
 * Created by xiangpi on 16/9/8.
 */
public interface IBlur {
    /**
     * 模糊操作
     * @param inBitmap 输入的bitmap
     * @return
     */
    Bitmap doBlur(Bitmap inBitmap);

    void setBlurMode(Blur.BlurMode mode);

    void setBlurRadius(int radius);

    void setSampleFactor(float factor);

}
