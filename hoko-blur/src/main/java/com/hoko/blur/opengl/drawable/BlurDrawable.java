package com.hoko.blur.opengl.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

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

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public BlurDrawable() {
        mBlurRenderer = new ScreenBlurRenderer.Builder().build();
        mDrawFunctor = new DrawFunctor(new DrawFunctor.DrawLocationObserver() {
            @Override
            public void onLocated(DrawFunctor.GLInfo info) {
                mBlurRenderer.onDrawFrame(info);
            }

            @Override
            public void onLocateError(int what) {
                mBlurEnabled = false;
                invalidateOnMainThread();
            }
        });
        mPaint = new Paint();
        mPaint.setColor(Color.TRANSPARENT);
    }

    @Override
    public void draw(Canvas canvas) {
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
        invalidateOnMainThread();
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

    private void invalidateOnMainThread() {
        Looper currentLooper = Looper.myLooper();
        if (currentLooper == null || !currentLooper.equals(Looper.getMainLooper())) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    invalidateSelf();
                }
            });
        } else {
            invalidateSelf();
        }
    }

    public void mode(@Mode int mode) {
        mBlurRenderer.mode(mode);
        invalidateOnMainThread();
    }

    public void radius(int radius) {
        mBlurRenderer.radius(radius);
        invalidateOnMainThread();
    }

    public void sampleFactor(float factor) {
        mBlurRenderer.sampleFactor(factor);
        invalidateOnMainThread();
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
