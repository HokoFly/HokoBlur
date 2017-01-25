package com.hoko.blurlibrary.opengl.texture;

import android.opengl.GLES20;

import com.hoko.blurlibrary.api.ITexture;

/**
 * Created by xiangpi on 17/1/20.
 */

public abstract class Texture implements ITexture {

    private int mTextureId;

    private int mWidth;

    private int mHeight;

    protected void genTexture() {

        final int[] textureIds = new int[1];

        GLES20.glGenTextures(1, textureIds, 0);

        mTextureId = textureIds[0];

        if (mTextureId != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            initTexture();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    protected abstract void initTexture();

    @Override
    public void delete() {
        if (mTextureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        }
    }

    @Override
    public void setId(int textureId) {
        mTextureId = textureId;
    }

    @Override
    public int getId() {
        return mTextureId;
    }

    @Override
    public void setWidth(int width) {
        mWidth = width;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public void setHeight(int height) {
        mHeight = height;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }
}
