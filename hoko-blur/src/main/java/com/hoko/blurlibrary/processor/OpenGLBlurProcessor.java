package com.hoko.blurlibrary.processor;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.opengl.cache.TextureCache;
import com.hoko.blurlibrary.opengl.offscreen.EglBuffer;
import com.hoko.blurlibrary.util.Preconditions;


/**
 * Created by yuxfzju on 16/9/7.
 */
class OpenGLBlurProcessor extends BlurProcessor {

    private EglBuffer mEglBuffer;

    OpenGLBlurProcessor(Builder builder) {
        super(builder);
        init();
    }

    private void init() {
        mEglBuffer = new EglBuffer();
    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        Preconditions.checkNotNull(scaledInBitmap, "scaledInBitmap == null");
        Preconditions.checkArgument(!scaledInBitmap.isRecycled(), "You must input an unrecycled bitmap !");

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