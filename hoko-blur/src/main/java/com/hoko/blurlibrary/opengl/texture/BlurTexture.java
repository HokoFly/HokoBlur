package com.hoko.blurlibrary.opengl.texture;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.Buffer;

/**
 * Created by xiangpi on 17/1/20.
 */

class BlurTexture extends Texture {

    BlurTexture(int width, int height) {
        super(width, height);
    }

    BlurTexture(Bitmap bitmap) {
        super(bitmap);
    }



    @Override
    protected void initTexture(int width, int height) {
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer) null);
    }

}
