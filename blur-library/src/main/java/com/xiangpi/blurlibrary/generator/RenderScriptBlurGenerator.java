package com.xiangpi.blurlibrary.generator;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.renderscript.ScriptC_boxblur;
import com.xiangpi.blurlibrary.renderscript.ScriptC_stackblur;

/**
 * Created by xiangpi on 16/9/7.
 */
public class RenderScriptBlurGenerator extends BlurGenerator {

//    private static volatile RenderScriptBlurGenerator sGenerator;

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mGaussianBlurScirpt;
    private ScriptC_boxblur mBoxBlurScript;
    private ScriptC_stackblur mStackBlurScript;

    private Allocation mAllocationIn;
    private Allocation mAllocationOut;

    public RenderScriptBlurGenerator(Context context) {
        init(context);

    }

    private void init(Context context) {
        mRenderScript = RenderScript.create(context.getApplicationContext());
        mGaussianBlurScirpt = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        mBoxBlurScript = new ScriptC_boxblur(mRenderScript);
        mStackBlurScript = new ScriptC_stackblur(mRenderScript);
    }

//    public static RenderScriptBlurGenerator getInstance(Context context) {
//        if (sGenerator == null) {
//            synchronized (RenderScriptBlurGenerator.class) {
//                if (sGenerator == null) {
//                    sGenerator = new RenderScriptBlurGenerator(context);
//                }
//            }
//        }
//
//        return sGenerator;
//    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap) {
        if (scaledInBitmap == null) {
            return null;
        }

        Bitmap scaledOutBitmap = Bitmap.createBitmap(scaledInBitmap.getWidth(), scaledInBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        mAllocationIn = Allocation.createFromBitmap(mRenderScript, scaledInBitmap);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, scaledOutBitmap);

        try {
            if (mBlurMode == Blur.BlurMode.BOX) {
                doBoxBlur(scaledInBitmap);
            } else if (mBlurMode == Blur.BlurMode.STACK) {
                doStackBlur(scaledInBitmap);
            } else if (mBlurMode == Blur.BlurMode.GAUSSIAN) {
                doGaussianBlur(scaledInBitmap);
            }

            mAllocationOut.copyTo(scaledOutBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            scaledOutBitmap = scaledInBitmap;
        }

        return scaledOutBitmap;
    }

//    public static void release() {
//        sGenerator = null;
//    }

    private void doBoxBlur(Bitmap input) {
        mBoxBlurScript.set_input(mAllocationIn);
        mBoxBlurScript.set_output(mAllocationOut);
        mBoxBlurScript.set_radius(mRadius);
        mBoxBlurScript.set_width(input.getWidth());
        mBoxBlurScript.set_height(input.getHeight());
        mBoxBlurScript.forEach_boxblur(mAllocationIn);
    }

    private void doGaussianBlur(Bitmap input) {
        // 模糊核半径太大，RenderScript失效，这里做发限制
        if (mRadius > 25) {
            mRadius = 25;
        }
        mGaussianBlurScirpt.setRadius(mRadius);
//        mAllocationIn.copyFrom(input);
        mGaussianBlurScirpt.setInput(mAllocationIn);
        mGaussianBlurScirpt.forEach(mAllocationOut);
    }

    private void doStackBlur(Bitmap input) {

        mStackBlurScript.set_input(mAllocationIn);
        mStackBlurScript.set_output(mAllocationOut);
        mStackBlurScript.set_width(input.getWidth());
        mStackBlurScript.set_height(input.getHeight());
        mStackBlurScript.set_radius(mRadius);
        mStackBlurScript.forEach_stackblur2_v(mAllocationIn);

        mStackBlurScript.set_input(mAllocationOut);
        mStackBlurScript.set_output(mAllocationIn);
        mStackBlurScript.forEach_stackblur2_h(mAllocationOut);
        mAllocationOut = mAllocationIn;
    }

}
