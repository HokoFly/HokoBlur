package com.hoko.blurlibrary.generator;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.opengl.cache.TextureCache;
import com.hoko.blurlibrary.opengl.offscreen.EglBuffer;


/**
 * Created by yuxfzju on 16/9/7.
 */
class OpenGLBlurGenerator extends BlurGenerator {

    private EglBuffer mEglBuffer;

    OpenGLBlurGenerator(BlurBuilder builder) {
        super(builder);
        init();
    }

    private void init() {
        mEglBuffer = new EglBuffer();
    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        if (scaledInBitmap == null || scaledInBitmap.isRecycled()) {
            return null;
        }

        // TODO: 2017/2/20 opengl 的并发处理
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