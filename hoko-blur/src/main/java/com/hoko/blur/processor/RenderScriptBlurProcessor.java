package com.hoko.blur.processor;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RSRuntimeException;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.renderscript.ScriptC_BoxBlur;
import com.hoko.blur.renderscript.ScriptC_StackBlur;
import com.hoko.blur.util.MathUtil;
import com.hoko.blur.util.Preconditions;

/**
 * Created by yuxfzju on 16/9/7.
 */
class RenderScriptBlurProcessor extends BlurProcessor {
    private static final String TAG = RenderScriptBlurProcessor.class.getSimpleName();

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mGaussianBlurScript;
    private ScriptC_BoxBlur mBoxBlurScript;
    private ScriptC_StackBlur mStackBlurScript;

    private static final int RS_MAX_RADIUS = 25;

    private volatile boolean rsRuntimeInited = false;

    RenderScriptBlurProcessor(HokoBlurBuild builder) {
        super(builder);
        init(builder.mCtx);
    }

    private void init(Context context) {
        Preconditions.checkNotNull(context, "Please set context for renderscript scheme, forget to set context for builder?");

        try {
            mRenderScript = RenderScript.create(context.getApplicationContext());
            mGaussianBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
            mBoxBlurScript = new ScriptC_BoxBlur(mRenderScript);
            mStackBlurScript = new ScriptC_StackBlur(mRenderScript);
            rsRuntimeInited = true;
        } catch (RSRuntimeException e) {
            Log.e(TAG, "Failed to init RenderScript runtime", e);
            rsRuntimeInited = false;
        }

    }


    /**
     * RenderScript built-in parallel implementation
     *
     * @param bitmap
     * @param concurrent
     * @return
     */
    @Override
    protected Bitmap doInnerBlur(Bitmap bitmap, boolean concurrent) {
        Preconditions.checkNotNull(bitmap, "scaledInBitmap == null");

        if (!rsRuntimeInited) {
            Log.e(TAG, "RenderScript Runtime is not initialized");
            return bitmap;
        }


        Allocation allocationIn = Allocation.createFromBitmap(mRenderScript, bitmap);
        Allocation allocationOut = Allocation.createFromBitmap(mRenderScript, Bitmap.createBitmap(bitmap));
        try {
            switch (mMode) {
                case HokoBlur.MODE_BOX:
                    doBoxBlur(bitmap, allocationIn, allocationOut);
                    allocationIn.copyTo(bitmap);

                    break;
                case HokoBlur.MODE_STACK:
                    doStackBlur(bitmap, allocationIn, allocationOut);
                    allocationIn.copyTo(bitmap);

                    break;
                case HokoBlur.MODE_GAUSSIAN:
                    doGaussianBlur(allocationIn, allocationOut);
                    allocationOut.copyTo(bitmap);

                    break;
            }

        } catch (Throwable e) {
            Log.e(TAG, "Blur the bitmap error", e);
        } finally {
            allocationIn.destroy();
            allocationOut.destroy();
        }

        return bitmap;
    }


    private void doBoxBlur(Bitmap input, Allocation in, Allocation out) {
        if (mBoxBlurScript == null) {
            throw new IllegalStateException("The blur script is unavailable");
        }

        mBoxBlurScript.set_input(in);
        mBoxBlurScript.set_output(out);
        mBoxBlurScript.set_width(input.getWidth());
        mBoxBlurScript.set_height(input.getHeight());
        mBoxBlurScript.set_radius(mRadius);
        mBoxBlurScript.forEach_boxblur_h(in);

        mBoxBlurScript.set_input(out);
        mBoxBlurScript.set_output(in);
        mBoxBlurScript.forEach_boxblur_v(out);

    }

    private void doGaussianBlur(Allocation in, Allocation out) {
        if (mGaussianBlurScript == null) {
            throw new IllegalStateException("The blur script is unavailable");
        }
        // RenderScript won't work, if too large blur radius
        mRadius = MathUtil.clamp(mRadius, 0, RS_MAX_RADIUS);
        mGaussianBlurScript.setRadius(mRadius);
//        mAllocationIn.copyFrom(input);
        mGaussianBlurScript.setInput(in);
        mGaussianBlurScript.forEach(out);
    }

    private void doStackBlur(Bitmap input, Allocation in, Allocation out) {
        if (mStackBlurScript == null) {
            throw new IllegalStateException("The blur script is unavailable");
        }

        mStackBlurScript.set_input(in);
        mStackBlurScript.set_output(out);
        mStackBlurScript.set_width(input.getWidth());
        mStackBlurScript.set_height(input.getHeight());
        mStackBlurScript.set_radius(mRadius);
        mStackBlurScript.forEach_stackblur_v(in);

        mStackBlurScript.set_input(out);
        mStackBlurScript.set_output(in);
        mStackBlurScript.forEach_stackblur_h(out);
    }

}
