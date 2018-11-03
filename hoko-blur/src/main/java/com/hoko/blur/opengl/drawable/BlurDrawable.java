package com.hoko.blur.opengl.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.hoko.blur.anno.Mode;
import com.hoko.blur.opengl.functor.DrawFunctor;
import com.hoko.blur.opengl.functor.ScreenBlurRenderer;

/**
 * Created by yuxfzju on 16/11/23.
 */
public class BlurDrawable extends Drawable {

    private DrawFunctor mDrawFunctor;

    private ScreenBlurRenderer mBlurRenderer;

    private int alpha;

    private Paint mPaint;
    private volatile boolean mBlurEnabled = true;

    public BlurDrawable() {
        mBlurRenderer = new ScreenBlurRenderer.Builder().build();
        mDrawFunctor = new DrawFunctor(mBlurRenderer);
        mPaint = new Paint();
        mPaint.setColor(Color.TRANSPARENT);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (canvas.isHardwareAccelerated() && mBlurEnabled) {
            boolean isSuccess = mDrawFunctor.doDraw(canvas);
            if (!isSuccess) {
                canvas.drawRect(getBounds(), mPaint);
            }
        } else {
            canvas.drawRect(getBounds(), mPaint);
        }
    }

    /**
     * todo setting Alpha is invalid
     */
    @Override
    @Deprecated
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    /**
     * todo setting ColorFilter is invalid
     */
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // TODO: 2017/1/25  
    }

    @Override
    public int getOpacity() {
        return alpha == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }


    public void disableBlur() {
        mBlurEnabled = false;
    }

    public void enableBlur() {
        mBlurEnabled = true;
    }

    public void mode(@Mode int mode) {
        mBlurRenderer.mode(mode);
        invalidateSelf();
    }

    public void radius(int radius) {
        mBlurRenderer.radius(radius);
        invalidateSelf();
    }

    public void sampleFactor(float factor) {
        mBlurRenderer.sampleFactor(factor);
        invalidateSelf();
    }

    public int mode() {
        return mBlurRenderer.mode();
    }

    public int radius() {
        return mBlurRenderer.radius();
    }

    public float sampleFactor() {
        return mBlurRenderer.sampleFactor();
    }

    public void freeGLResource() {
        mBlurRenderer.free();
    }

}
