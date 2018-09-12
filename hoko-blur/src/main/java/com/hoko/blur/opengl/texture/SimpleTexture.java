package com.hoko.blur.opengl.texture;

import android.opengl.GLES20;

import com.hoko.blur.util.Preconditions;

import java.nio.Buffer;

/**
 * Created by yuxfzju on 17/1/20.
 */

class SimpleTexture extends Texture {

    SimpleTexture(int width, int height) {
        width(width);
        height(height);
        genTexture();
    }

    @Override
    protected void initTexture() {
        Preconditions.checkArgument(width() > 0 && height() > 0, "width > 0 and height > 0");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width(), height(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer) null);
    }

}
