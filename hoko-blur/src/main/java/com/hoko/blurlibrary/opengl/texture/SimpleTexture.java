package com.hoko.blurlibrary.opengl.texture;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.Buffer;

/**
 * Created by xiangpi on 17/1/20.
 */

class SimpleTexture extends Texture {

    SimpleTexture(int width, int height) {
        setWidth(width);
        setHeight(height);
        genTexture();
    }

    @Override
    protected void initTexture() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, getWidth(), getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer) null);
    }

}
