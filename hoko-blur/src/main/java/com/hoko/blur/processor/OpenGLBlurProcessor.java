package com.hoko.blur.processor;

import android.graphics.Bitmap;

import com.hoko.blur.opengl.offscreen.EglBuffer;
import com.hoko.blur.util.Preconditions;


/**
 * Created by yuxfzju on 16/9/7.
 */
class OpenGLBlurProcessor extends BlurProcessor {
    private static final String TAG = OpenGLBlurProcessor.class.getSimpleName();
    private final EglBuffer mEglBuffer = new EglBuffer();

    OpenGLBlurProcessor(HokoBlurBuild builder) {
        super(builder);
    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        Preconditions.checkNotNull(scaledInBitmap, "scaledInBitmap == null");
        Preconditions.checkArgument(!scaledInBitmap.isRecycled(), "You must input an unrecycled bitmap !");
        return mEglBuffer.getBlurBitmap(scaledInBitmap, mRadius, mMode);

    }

}