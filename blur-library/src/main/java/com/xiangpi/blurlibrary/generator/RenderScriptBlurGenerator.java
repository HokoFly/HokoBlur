package com.xiangpi.blurlibrary.generator;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.example.xiangpi.dynamicblurdemo.activity.ScriptC_boxblur;
import com.example.xiangpi.dynamicblurdemo.activity.ScriptC_stackblur;
import com.xiangpi.blurlibrary.Blur;

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
    public Bitmap doBlur(Bitmap input) {
        if (input == null) {
            throw new IllegalArgumentException("You must input a bitmap !");
        }

        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);

        mAllocationIn = Allocation.createFromBitmap(mRenderScript, input);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, output);

        try {
            if (mBlurMode == Blur.BlurMode.BOX) {
                doBoxBlur(input);
            } else if (mBlurMode == Blur.BlurMode.STACK) {
                doStackBlur(input);
            } else if (mBlurMode == Blur.BlurMode.GAUSSIAN) {
                doGaussianBlur(input);
            }

            mAllocationOut.copyTo(output);
        } catch (Exception e) {
            e.printStackTrace();
            output = input;
        }

        return output;
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
        mGaussianBlurScirpt.setRadius(mRadius);
        mAllocationIn.copyFrom(input);
        mGaussianBlurScirpt.setInput(mAllocationIn);
        mGaussianBlurScirpt.forEach(mAllocationOut);
    }

    private void doStackBlur(Bitmap input) {
        mStackBlurScript.set_input(mAllocationIn);
        mStackBlurScript.set_output(mAllocationOut);
        mStackBlurScript.set_width(input.getWidth());
        mStackBlurScript.set_height(input.getHeight());
        mStackBlurScript.set_radius(mRadius);

        int[] rowIndices = new int[input.getHeight()];
        int[] colIndices = new int[input.getWidth()];

        for (int i = 0; i < input.getHeight(); i++) {
            rowIndices[i] = i;
        }

        for (int i = 0; i < input.getWidth(); i++) {
            colIndices[i] = i;
        }

        Allocation rows = Allocation.createSized(mRenderScript, Element.U32(mRenderScript), input.getHeight(), Allocation.USAGE_SCRIPT);
        Allocation cols = Allocation.createSized(mRenderScript, Element.U32(mRenderScript), input.getWidth(), Allocation.USAGE_SCRIPT);

        rows.copyFrom(rowIndices);
        cols.copyFrom(colIndices);
        mStackBlurScript.forEach_blur_h(rows);
        mStackBlurScript.forEach_blur_v(cols);
    }

}
