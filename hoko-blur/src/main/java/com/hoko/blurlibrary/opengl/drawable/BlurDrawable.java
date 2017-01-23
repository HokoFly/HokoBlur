package com.hoko.blurlibrary.opengl.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.generator.IBlur;
import com.hoko.blurlibrary.opengl.functor.DrawFunctor;

/**
 * Created by xiangpi on 16/11/23.
 */
public class BlurDrawable extends Drawable implements IBlur{

    private DrawFunctor mDrawFunctor;

    private int alpha;

    private Paint mPaint;

    public BlurDrawable() {
        mDrawFunctor = new DrawFunctor();
        mPaint = new Paint();
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas.isHardwareAccelerated() && getBlurRadius() > 0) {
            mDrawFunctor.doDraw(canvas);
        } else {
            mPaint.setColor(Color.TRANSPARENT);
            canvas.drawRect(getBounds(), mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return alpha == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setBlurMode(@Blur.BlurMode int mode) {
        mDrawFunctor.setBlurMode(mode);
    }

    @Override
    public void setBlurRadius(int radius) {
        mDrawFunctor.setBlurRadius(radius);
    }

    @Override
    public void setSampleFactor(float factor) {
        mDrawFunctor.setSampleFactor(factor);
    }

    @Override
    public int getBlurMode() {
        return mDrawFunctor.getBlurMode();
    }

    @Override
    public int getBlurRadius() {
        return mDrawFunctor.getBlurRadius();
    }

    @Override
    public float getSampleFactor() {
        return mDrawFunctor.getSampleFactor();
    }
}
