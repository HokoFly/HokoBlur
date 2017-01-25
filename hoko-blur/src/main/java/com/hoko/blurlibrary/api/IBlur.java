package com.hoko.blurlibrary.api;

import com.hoko.blurlibrary.Blur;

/**
 * Created by xiangpi on 2017/1/23.
 */
public interface IBlur {
    void setBlurMode(@Blur.BlurMode int mode);

    void setBlurRadius(int radius);

    void setSampleFactor(float factor);

    @Blur.BlurMode
    int getBlurMode();

    int getBlurRadius();

    float getSampleFactor();
}
