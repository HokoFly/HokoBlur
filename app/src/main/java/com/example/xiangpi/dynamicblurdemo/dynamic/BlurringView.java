package com.example.xiangpi.dynamicblurdemo.dynamic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.EventLogTags;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by xiangpi on 16/8/20.
 */
public class BlurringView extends View {

    private static final int BLUR_KERNEL_RADIUS = 5;


    private float mOldX;
    private float mOldY;

    private View mBlurredView;

    private Bitmap mToBlurBitmap;
    private Bitmap mBlurredBitmap;
    private Canvas mBlurringCanvas;

    private Allocation mBlurAllocIn;
    private Allocation mBlurAllocOut;
    private RenderScript mRenderScript;

    private ScriptIntrinsicBlur mGaussianBlurScirpt;

    public BlurringView(Context context) {
        super(context);
        init(context);

    }

    public BlurringView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        mRenderScript = RenderScript.create(context);
        mGaussianBlurScirpt = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        mGaussianBlurScirpt.setRadius(BLUR_KERNEL_RADIUS);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBlurredView != null) {

            if (prepare()) {

                if (mBlurredView.getBackground() != null && mBlurredView.getBackground() instanceof ColorDrawable) {
                    mToBlurBitmap.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());
                } else {
                    mToBlurBitmap.eraseColor(Color.TRANSPARENT);
                }

                mBlurredView.draw(mBlurringCanvas);
                doBlur();

                canvas.save();
                canvas.translate(mBlurredView.getX() - getX(), mBlurredView.getY() - getY());
//                canvas.scale(mDownsampleFactor, mDownsampleFactor);
                canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
                canvas.restore();
            }

        }


    }

    private boolean prepare() {
        final int width = mBlurredView.getWidth();
        final int height = mBlurredView.getHeight();

        if (mBlurringCanvas == null) {

            if (mToBlurBitmap == null) {
                mToBlurBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }

            if (mToBlurBitmap == null) {
                return false;
            }

            if (mBlurredBitmap == null) {
                mBlurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }

            if (mBlurredBitmap == null) {
                return false;
            }

            mBlurringCanvas = new Canvas(mToBlurBitmap);

            mBlurAllocIn = Allocation.createFromBitmap(mRenderScript, mToBlurBitmap);
            mBlurAllocOut = Allocation.createFromBitmap(mRenderScript, mBlurredBitmap);
        }

        return true;
    }

    private void doBlur() {
        mBlurAllocIn.copyFrom(mToBlurBitmap);
        mGaussianBlurScirpt.setInput(mBlurAllocIn);
        mGaussianBlurScirpt.forEach(mBlurAllocOut);
        mBlurAllocOut.copyTo(mBlurredBitmap);
    }

    public void setBlurredView(View blurredView) {
        mBlurredView = blurredView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mOldX = event.getRawX();
                mOldY = event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - mOldX;
                float dy = event.getRawY() - mOldY;
                ViewCompat.offsetLeftAndRight(this, (int) dx);
                ViewCompat.offsetTopAndBottom(this, (int) dy);
//                setLeft((int) (getLeft() + dx));
//                setRight((int) (getRight() + dx));
//                setTop((int) (getTop() + dy));
//                setBottom((int) (getBottom() + dy));
//                layout(getLeft() + (int)dx, getTop() + (int)dy, getRight() + (int)dx, getBottom() + (int)dy);
//                ((ViewGroup)getParent()).scrollBy((int)dx, (int)dy);
                mOldX = event.getRawX();
                mOldY = event.getRawY();
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;

        }


        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRenderScript != null) {
            mRenderScript.destroy();
        }
    }
}
