package com.hoko.blur.processor;

import android.graphics.Bitmap;

import com.hoko.blur.opengl.cache.FrameBufferCache;
import com.hoko.blur.opengl.cache.TextureCache;
import com.hoko.blur.opengl.offscreen.EglBuffer;
import com.hoko.blur.util.Preconditions;


/**
 * Created by yuxfzju on 16/9/7.
 */
class OpenGLBlurProcessor extends BlurProcessor {
    private static final String TAG = OpenGLBlurProcessor.class.getSimpleName();

    private final EglBuffer mEglBuffer;

    OpenGLBlurProcessor(Builder builder) {
        super(builder);
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
    }

    @Override
    protected void finalize() throws Throwable {
        free();
        super.finalize();
    }
}