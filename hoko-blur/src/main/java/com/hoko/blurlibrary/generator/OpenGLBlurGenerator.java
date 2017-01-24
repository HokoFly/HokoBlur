package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.opengl.cache.TextureCache;
import com.hoko.blurlibrary.opengl.offscreen.GLRenderer;
import com.hoko.blurlibrary.opengl.offscreen.OffScreenBuffer;
import com.hoko.blurlibrary.opengl.offscreen.OffScreenRendererImpl;


/**
 * Created by xiangpi on 16/9/7.
 */
public class OpenGLBlurGenerator extends BitmapBlurGenerator {

//    private static volatile OpenGLBlurGenerator sGenerator;

    private OffScreenRendererImpl mGLRenderer;

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
        if (mGLRenderer == null) {
            mGLRenderer = new OffScreenRendererImpl(scaledInBitmap);
        }
        mGLRenderer.setBlurRadius(mRadius);
        mGLRenderer.setBlurMode(mMode);
        mOffScreenBuffer.setRenderer(mGLRenderer);
        return mOffScreenBuffer.getBitmap();
    }

//    public static void release() {
//        sGenerator = null;
//    }


    @Override
    protected void free() {
        if (mGLRenderer != null) {
            mGLRenderer.free();
        }

        TextureCache.getInstance().deleteTextures();
        FrameBufferCache.getInstance().deleteFrameBuffers();

    }
}
