package com.hoko.blurlibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.hoko.blurlibrary.HokoBlur;
import com.hoko.blurlibrary.api.IBlurGenerator;

/**
 * Created by yuxfzju on 16/9/18.
 */
public class RsBlurLinearLayout extends LinearLayout {

    private static final int DEFAULT_BLUR_RADIUS = 5;

    private static final float DEFAULT_BITMAP_SAMPLE_FACTOR = 5.0f;

    private int[] mLocationInWindow;

    private IBlurGenerator mGenerator;

    private Bitmap mBitmap;

    private Canvas mCanvas;

    private final ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (getVisibility() == View.VISIBLE) {
                prepare();
            }
            return true;
        }
    };

    public RsBlurLinearLayout(Context context) {
        super(context);
        init();
    }

    public RsBlurLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RsBlurLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCanvas = new Canvas();
        mLocationInWindow = new int[2];
        mGenerator = HokoBlur.with(getContext()).scheme(HokoBlur.SCHEME_RENDER_SCRIPT).sampleFactor(DEFAULT_BITMAP_SAMPLE_FACTOR).blurGenerator();
        setBlurRadius(DEFAULT_BLUR_RADIUS);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mCanvas == canvas) {
            mBitmap = mGenerator.blur(mBitmap);
        } else {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, new Matrix(), null);
            }
            super.dispatchDraw(canvas);
        }
    }

    public void setBlurRadius(int radius) {
        mGenerator.radius(radius);
        invalidate();
    }

    public void setSampleFactor(float factor) {
        mGenerator.sampleFactor(factor);
        invalidate();
    }

    private void prepare() {
        int width = getWidth();
        int height = getHeight();

        width = Math.max(width, 1);
        height = Math.max(height, 1);

        if (mBitmap == null || mBitmap.getWidth() != width || mBitmap.getHeight() != height) {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        getLocationInWindow(mLocationInWindow);
        mCanvas.restoreToCount(1);
        mCanvas.setBitmap(mBitmap);
        mCanvas.setMatrix(new Matrix());
        mCanvas.translate(-mLocationInWindow[0], -mLocationInWindow[1]);
        mCanvas.save();
        getRootView().draw(mCanvas);
    }

}