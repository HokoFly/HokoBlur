package com.hoko.blur.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.lang.ref.WeakReference;

/**
 * Created by yuxfzju on 2017/1/24.
 */

class BitmapTexture extends Texture {

    private final WeakReference<Bitmap> mBitmapWeakRef;

    BitmapTexture(Bitmap bitmap) {
        super(bitmap.getWidth(), bitmap.getHeight());
        mBitmapWeakRef = new WeakReference<>(bitmap);
        create();
    }

    @Override
    protected void onTextureCreated() {
        if (width() != 0 && height() != 0 && mBitmapWeakRef != null) {
            Bitmap bitmap = mBitmapWeakRef.get();
            if (bitmap != null && !bitmap.isRecycled()) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            }
        }

    }
}
