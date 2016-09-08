package com.example.xiangpi.dynamicblurdemo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;

import com.example.xiangpi.dynamicblurdemo.opengl.GLRenderer;
import com.example.xiangpi.dynamicblurdemo.opengl.offline.OffScreenBuffer;
import com.example.xiangpi.dynamicblurdemo.opengl.offline.OffScreenRendererImpl;

/**
 * Created by xiangpi on 16/9/7.
 */
public class OpenGLBlurHelper {

    private static volatile OpenGLBlurHelper sHelper;

    private static final int BLUR_KERNEL_RADIUS = 5;

    private GLRenderer mGLRenderer;

    private OffScreenBuffer mOffScreenBuffer;

    private Context mCtx;

    private OpenGLBlurHelper(Context context) {
        init(context);

    }

    private void init(Context context) {
        mOffScreenBuffer = new OffScreenBuffer(context);
        mCtx = context;
    }

    public static OpenGLBlurHelper getInstance(Context context) {
        if (sHelper == null) {
            synchronized (OpenGLBlurHelper.class) {
                if (sHelper == null) {
                    sHelper = new OpenGLBlurHelper(context);
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
        mGLRenderer = new OffScreenRendererImpl(mCtx, input, radius);

        mOffScreenBuffer.setRenderer(mGLRenderer);

        return mOffScreenBuffer.getBitmap();
    }

    public static void release() {
        sHelper = null;
    }



}
