package com.hoko.blurlibrary.opengl.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.hoko.blurlibrary.opengl.functor.DrawFunctor;

/**
 * Created by xiangpi on 16/11/23.
 */
public class BlurDrawable extends Drawable {

    private DrawFunctor mDrawFunctor;

    private int alpha;

    private Paint mPaint;

    public BlurDrawable() {
        mDrawFunctor = new DrawFunctor();
        mPaint = new Paint();
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas.isHardwareAccelerated()) {
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

}
