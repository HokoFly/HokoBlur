package com.hoko.blur.processor;

import android.graphics.Bitmap;

import com.hoko.blur.opengl.offscreen.EglBuffer;
import com.hoko.blur.util.Preconditions;


/**
 * Created by yuxfzju on 16/9/7.
 */
class OpenGLBlurProcessor extends BlurProcessor {
    private static final String TAG = OpenGLBlurProcessor.class.getSimpleName();

    private final EglBuffer mEglBuffer;

    OpenGLBlurProcessor(HokoBlurBuild builder) {
        super(builder);
        mEglBuffer = new EglBuffer();

    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        Preconditions.checkNotNull(scaledInBitmap, "scaledInBitmap == null");
        Preconditions.checkArgument(!scaledInBitmap.isRecycled(), "You must input an unrecycled bitmap !");

        // TODO: 2017/2/20 opengl process parallel
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