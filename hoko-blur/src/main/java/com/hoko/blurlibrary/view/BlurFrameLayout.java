package com.hoko.blurlibrary.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.hoko.blurlibrary.opengl.drawable.BlurDrawable;

/**
 * Created by xiangpi on 16/11/9.
 */
public class BlurFrameLayout extends FrameLayout{
    private BlurDrawable mBlurDrawable;

    public BlurFrameLayout(Context context) {
        super(context);
        init();
    }

    public BlurFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlurFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBlurDrawable = new BlurDrawable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mBlurDrawable);
        } else {
            setBackgroundDrawable(mBlurDrawable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBlurDrawable.freeGLResource();
    }

    public BlurDrawable getBlurDrawable() {
        return mBlurDrawable;
    }
}
