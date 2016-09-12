package com.xiangpi.blurlibrary.generator;

import android.graphics.Bitmap;

import com.xiangpi.blurlibrary.opengl.offscreen.GLRenderer;
import com.xiangpi.blurlibrary.opengl.offscreen.OffScreenBuffer;
import com.xiangpi.blurlibrary.opengl.offscreen.OffScreenRendererImpl;
import com.xiangpi.blurlibrary.util.BitmapUtil;


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
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap) {
        if (scaledInBitmap == null) {
            return null;
        }
        mGLRenderer = new OffScreenRendererImpl(scaledInBitmap, mRadius, mBlurMode);
        mOffScreenBuffer.setRenderer(mGLRenderer);
        return mOffScreenBuffer.getBitmap();
    }

//    public static void release() {
//        sGenerator = null;
//    }

}
