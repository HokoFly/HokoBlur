package com.hoko.blur.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.hoko.blur.util.Preconditions;

/**
 * Created by yuxfzju on 17/1/20.
 */

public abstract class Texture {

    private int mTextureId;

    private int mWidth;

    private int mHeight;

    public static Texture create(int width, int height) {
        Preconditions.checkArgument(width > 0 && height > 0, "width > 0 and height > 0");
        return new SimpleTexture(width, height);
    }

    public static Texture create(Bitmap bitmap) {
        Preconditions.checkNotNull(bitmap, "bitmap == null");
        Preconditions.checkArgument(!bitmap.isRecycled(), "bitmap is recycled");
        return new BitmapTexture(bitmap);
    }

    public Texture(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    protected void create() {
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

    public void delete() {
        if (mTextureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        }
    }

    public int id() {
        return mTextureId;
    }

    public int width() {
        return mWidth;
    }

    public int height() {
        return mHeight;
    }
}
