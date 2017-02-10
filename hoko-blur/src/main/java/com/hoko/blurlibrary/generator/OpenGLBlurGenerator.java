package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.opengl.cache.TextureCache;
import com.hoko.blurlibrary.opengl.offscreen.EglBuffer;


/**
 * Created by xiangpi on 16/9/7.
 */
public class OpenGLBlurGenerator extends BlurGenerator {

    private EglBuffer mEglBuffer;

    public OpenGLBlurGenerator() {
        init();
    }

    private void init() {
        mEglBuffer = new EglBuffer();
    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap) {
        if (scaledInBitmap == null || scaledInBitmap.isRecycled()) {
            return null;
        }

        mEglBuffer.setBlurRadius(mRadius);
        mEglBuffer.setBlurMode(mMode);
        return mEglBuffer.getBlurBitmap(scaledInBitmap);
    }

    @Override
    protected void free() {
        mEglBuffer.free();
        TextureCache.getInstance().deleteTextures();
        FrameBufferCache.getInstance().deleteFrameBuffers();

    }
}
