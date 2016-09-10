package com.xiangpi.blurlibrary.generator;

import android.graphics.Bitmap;

import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.opengl.offscreen.GLRenderer;
import com.xiangpi.blurlibrary.opengl.offscreen.OffScreenBuffer;
import com.xiangpi.blurlibrary.opengl.offscreen.OffScreenRendererImpl;


/**
 * Created by xiangpi on 16/9/7.
 */
public class OpenGLBlurGenerator extends BlurGenerator{

//    private static volatile OpenGLBlurGenerator sGenerator;

    private GLRenderer mGLRenderer;

    private OffScreenBuffer mOffScreenBuffer;

    public OpenGLBlurGenerator() {
        init();
    }

    private void init() {
        mOffScreenBuffer = new OffScreenBuffer();
    }

//    public static OpenGLBlurGenerator getInstance() {
//        if (sGenerator == null) {
//            synchronized (OpenGLBlurGenerator.class) {
//                if (sGenerator == null) {
//                    sGenerator = new OpenGLBlurGenerator();
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

        mGLRenderer = new OffScreenRendererImpl(input, mRadius, mBlurMode);

        mOffScreenBuffer.setRenderer(mGLRenderer);

        return mOffScreenBuffer.getBitmap();
    }

//    public static void release() {
//        sGenerator = null;
//    }

}
