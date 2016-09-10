package com.xiangpi.blurlibrary.generator;

import com.xiangpi.blurlibrary.Blur;

/**
 * Created by xiangpi on 16/9/8.
 */
public abstract class BlurGenerator implements IBlur {

    private static final int BLUR_KERNEL_RADIUS = 5;

    private static final Blur.BlurMode DEFAULT_BLUR_MODE = Blur.BlurMode.GAUSSIAN;

    protected int mRadius = BLUR_KERNEL_RADIUS;

    protected Blur.BlurMode mBlurMode = DEFAULT_BLUR_MODE;

    @Override
    public void setBlurMode(Blur.BlurMode mode) {
        mBlurMode = mode;
    }

    @Override
    public void setBlurRadius(int radius) {
        mRadius = radius;
    }
}
