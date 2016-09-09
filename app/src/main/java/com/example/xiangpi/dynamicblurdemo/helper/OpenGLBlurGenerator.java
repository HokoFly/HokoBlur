package com.example.xiangpi.dynamicblurdemo.helper;

import android.graphics.Bitmap;

import com.example.xiangpi.dynamicblurdemo.opengl.GLRenderer;
import com.example.xiangpi.dynamicblurdemo.opengl.offline.OffScreenBuffer;
import com.example.xiangpi.dynamicblurdemo.opengl.offline.OffScreenRendererImpl;

/**
 * Created by xiangpi on 16/9/7.
 */
public class OpenGLBlurGenerator extends BlurGenerator{

    private static volatile OpenGLBlurGenerator sGenerator;

    private GLRenderer mGLRenderer;

    private OffScreenBuffer mOffScreenBuffer;

    private OpenGLBlurGenerator() {
        init();
    }

    private void init() {
        mOffScreenBuffer = new OffScreenBuffer();
    }

    public static OpenGLBlurGenerator getInstance() {
        if (sGenerator == null) {
            synchronized (OpenGLBlurGenerator.class) {
                if (sGenerator == null) {
                    sGenerator = new OpenGLBlurGenerator();
                }
            }
        }

        return sGenerator;
    }

    @Override
    public Bitmap doBlur(Bitmap input) {
        if (input == null) {
            throw new IllegalArgumentException("You must input a bitmap !");
        }

        mGLRenderer = new OffScreenRendererImpl(input, mRadius, mBlurMode);

        mOffScreenBuffer.setRenderer(mGLRenderer);

        return mOffScreenBuffer.getBitmap();
    }

    public static void release() {
        sGenerator = null;
    }


    @Override
    public void setBlurMode(Blur.BlurMode mode) {
        mBlurMode = mode;
    }

    @Override
    public void setBlurRadius(int radius) {
        mRadius = radius;
    }
}
