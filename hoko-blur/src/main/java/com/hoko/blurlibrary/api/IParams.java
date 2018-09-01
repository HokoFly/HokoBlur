package com.hoko.blurlibrary.api;

import com.hoko.blurlibrary.anno.Mode;

/**
 * Created by yuxfzju on 2017/1/23.
 */
public interface IParams {
    void mode(@Mode int mode);

    void radius(int radius);

    void sampleFactor(float sampleFactor);

    @Mode
    int mode();

    int radius();

    float sampleFactor();
}
