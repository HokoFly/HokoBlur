package com.hoko.blur.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RSRuntimeException;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.renderscript.ScriptC_BoxblurHorizontal;
import com.hoko.blur.renderscript.ScriptC_BoxblurVertical;
import com.hoko.blur.renderscript.ScriptC_Stackblur;
import com.hoko.blur.util.MathUtil;
import com.hoko.blur.util.Preconditions;

/**
 * Created by yuxfzju on 16/9/7.
 */
class RenderScriptBlurProcessor extends BlurProcessor {
    private static final String TAG = RenderScriptBlurProcessor.class.getSimpleName();

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mGaussianBlurScirpt;
    private ScriptC_BoxblurHorizontal mBoxBlurScriptH;
    private ScriptC_BoxblurVertical mBoxBlurScriptV;
    private ScriptC_Stackblur mStackBlurScript;

    private Allocation mAllocationIn;
    private Allocation mAllocationOut;

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
            mGaussianBlurScirpt = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
            mBoxBlurScriptH = new ScriptC_BoxblurHorizontal(mRenderScript);
            mBoxBlurScriptV = new ScriptC_BoxblurVertical(mRenderScript);
            mStackBlurScript = new ScriptC_Stackblur(mRenderScript);
            rsRuntimeInited = true;
        } catch (RSRuntimeException e) {
            Log.e(TAG, "Failed to init RenderScript runtime", e);
            rsRuntimeInited = false;
        }

    }


    /**
     * RenderScript built-in parallel implementation
     *
     * @param scaledInBitmap
     * @param concurrent
     * @return
     */
    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        Preconditions.checkNotNull(scaledInBitmap, "scaledInBitmap == null");

        if (!rsRuntimeInited) {
            Log.e(TAG, "RenderScript Runtime is not initialized");
            return scaledInBitmap;
        }

        Bitmap scaledOutBitmap = Bitmap.createBitmap(scaledInBitmap.getWidth(), scaledInBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        mAllocationIn = Allocation.createFromBitmap(mRenderScript, scaledInBitmap);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, scaledOutBitmap);
        try {
            switch (mMode) {
                case HokoBlur.MODE_BOX:
                    doBoxBlur(scaledInBitmap);
                    break;
                case HokoBlur.MODE_STACK:
                    doStackBlur(scaledInBitmap);
                    break;
                case HokoBlur.MODE_GAUSSIAN:
                    doGaussianBlur(scaledInBitmap);
                    break;
            }

            mAllocationOut.copyTo(scaledInBitmap);
        } catch (Throwable e) {
            Log.e(TAG, "Blur the bitmap error", e);
        } finally {
            mAllocationIn.destroy();
            mAllocationOut.destroy();
        }



        return scaledInBitmap;
    }


    private void doBoxBlur(Bitmap input) {
        if (mBoxBlurScriptH == null || mBoxBlurScriptV == null) {
            throw new IllegalStateException("The blur script is unavailable");
        }

        Allocation in = mAllocationIn;
        Allocation out = mAllocationOut;
        mBoxBlurScriptH.set_input(in);
        mBoxBlurScriptH.set_output(out);
        mBoxBlurScriptH.set_width(input.getWidth());
        mBoxBlurScriptH.set_height(input.getHeight());
        mBoxBlurScriptH.set_radius(mRadius);
        mBoxBlurScriptH.forEach_boxblur_h(in);

        mBoxBlurScriptV.set_input(out);
        mBoxBlurScriptV.set_output(in);
        mBoxBlurScriptV.set_width(input.getWidth());
        mBoxBlurScriptV.set_height(input.getHeight());
        mBoxBlurScriptV.set_radius(mRadius);
        mBoxBlurScriptV.forEach_boxblur_v(out);

        mAllocationIn = out;
        mAllocationOut = in;
    }

    private void doGaussianBlur(Bitmap input) {
        if (mGaussianBlurScirpt == null) {
            throw new IllegalStateException("The blur script is unavailable");
        }
        // RenderScript won't work, if too large blur radius
        mRadius = MathUtil.clamp(mRadius, 0, RS_MAX_RADIUS);
        mGaussianBlurScirpt.setRadius(mRadius);
//        mAllocationIn.copyFrom(input);
        mGaussianBlurScirpt.setInput(mAllocationIn);
        mGaussianBlurScirpt.forEach(mAllocationOut);
    }

    private void doStackBlur(Bitmap input) {
        if (mStackBlurScript == null) {
            throw new IllegalStateException("The blur script is unavailable");
        }

        Allocation in = mAllocationIn;
        Allocation out = mAllocationOut;

        mStackBlurScript.set_input(in);
        mStackBlurScript.set_output(out);
        mStackBlurScript.set_width(input.getWidth());
        mStackBlurScript.set_height(input.getHeight());
        mStackBlurScript.set_radius(mRadius);
        mStackBlurScript.forEach_stackblur_v(in);

        mStackBlurScript.set_input(out);
        mStackBlurScript.set_output(in);
        mStackBlurScript.forEach_stackblur_h(out);

        mAllocationIn = out;
        mAllocationOut = in;
    }

}
