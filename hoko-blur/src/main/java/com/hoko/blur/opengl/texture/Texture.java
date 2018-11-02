package com.hoko.blur.opengl.texture;

import android.opengl.GLES20;

import com.hoko.blur.api.ITexture;

/**
 * Created by yuxfzju on 17/1/20.
 */

public abstract class Texture implements ITexture {

    private int mTextureId;

    private int mWidth;

    private int mHeight;

    @Override
    public void create() {

        final int[] textureIds = new int[1];

        GLES20.glGenTextures(1, textureIds, 0);

        mTextureId = textureIds[0];

        if (mTextureId != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            onTextureCreated();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    protected abstract void onTextureCreated();

    @Override
    public void delete() {
        if (mTextureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        }
    }

    public void id(int textureId) {
        mTextureId = textureId;
    }

    public int id() {
        return mTextureId;
    }

    public void width(int width) {
        mWidth = width;
    }

    public int width() {
        return mWidth;
    }

    public void height(int height) {
        mHeight = height;
    }

    public int height() {
        return mHeight;
    }
}
