package com.example.xiangpi.dynamicblurdemo.helper;

/**
 * Created by xiangpi on 16/9/8.
 */
public abstract class BlurGenerator implements IBlur {

    private static final int BLUR_KERNEL_RADIUS = 5;

    private static final Blur.BlurMode DEFAULT_BLUR_MODE = Blur.BlurMode.GAUSSIAN;

    protected int mRadius = BLUR_KERNEL_RADIUS;

    protected Blur.BlurMode mBlurMode = DEFAULT_BLUR_MODE;
}
