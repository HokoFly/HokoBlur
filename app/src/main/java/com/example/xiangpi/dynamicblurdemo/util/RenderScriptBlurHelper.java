package com.example.xiangpi.dynamicblurdemo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

/**
 * Created by xiangpi on 16/9/7.
 */
public class RenderScriptBlurHelper {

    private static volatile RenderScriptBlurHelper sHelper;

    private static final int BLUR_KERNEL_RADIUS = 5;

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mGaussianBlurScirpt;

    private Allocation mAllocationIn;
    private Allocation mAllocationOut;

    private RenderScriptBlurHelper(Context context) {
        init(context);

    }

    private void init(Context context) {
        mRenderScript = RenderScript.create(context);
        mGaussianBlurScirpt = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
    }

    public static RenderScriptBlurHelper getInstance(Context context) {
        if (sHelper == null) {
            synchronized (RenderScriptBlurHelper.class) {
                if (sHelper == null) {
                    sHelper = new RenderScriptBlurHelper(context);
                }
            }
        }

        return sHelper;
    }

    public Bitmap doBlur(Bitmap input, int radius) {
        if (input == null) {
            throw new IllegalArgumentException("You must input a bitmap !");
        }

        if (radius <= 0) {
            radius = BLUR_KERNEL_RADIUS;
        }
        mGaussianBlurScirpt.setRadius(radius);

        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);

        mAllocationIn = Allocation.createFromBitmap(mRenderScript, input);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, output);

        mAllocationIn.copyFrom(input);
        mGaussianBlurScirpt.setInput(mAllocationIn);
        mGaussianBlurScirpt.forEach(mAllocationOut);
        mAllocationOut.copyTo(output);


        return output;
    }

    public static void release() {
        sHelper = null;
    }
    

}
