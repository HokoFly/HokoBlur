package com.hoko.blurlibrary.opengl.texture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.lang.ref.WeakReference;

/**
 * Created by xiangpi on 2017/1/24.
 */

public class BitmapTexture extends Texture {

    private WeakReference<Bitmap> mBitmapWeakRef;

    BitmapTexture(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            setWidth(bitmap.getWidth());
            setHeight(bitmap.getHeight());
            mBitmapWeakRef = new WeakReference<Bitmap>(bitmap);
            genTexture();
        }
    }

    @Override
    protected void initTexture() {
        if (getWidth() != 0 && getHeight() != 0 && mBitmapWeakRef != null ) {
            Bitmap bitmap = mBitmapWeakRef.get();

            if (bitmap != null && !bitmap.isRecycled()) {
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            }

        }

    }
}
