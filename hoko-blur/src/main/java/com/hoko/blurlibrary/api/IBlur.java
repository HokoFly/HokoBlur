package com.hoko.blurlibrary.api;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.anno.Mode;

/**
 * Created by xiangpi on 2017/1/23.
 */
public interface IBlur {
    void setBlurMode(@Mode int mode);

    void setBlurRadius(int radius);

    void setSampleFactor(float factor);

    @Mode
    int getBlurMode();

    int getBlurRadius();

    float getSampleFactor();
}
