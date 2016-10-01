package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.Blur;

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

    void setBlurMode(@Blur.BlurMode int mode);

    void setBlurRadius(int radius);

    void setSampleFactor(float factor);

    @Blur.BlurMode int getBlurMode();

    int getBlurRadius();

    float getSampleFactor();

}
