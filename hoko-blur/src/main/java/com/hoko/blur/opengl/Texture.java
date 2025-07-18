package com.hoko.blur.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.hoko.blur.util.Preconditions;

import java.nio.Buffer;

/**
 * Created by yuxfzju on 17/1/20.
 */

public class Texture {

    private int mTextureId;

    private final int mWidth;

    private final int mHeight;

    private volatile boolean deleted = false;

    public static Texture create(int width, int height) {
        Preconditions.checkArgument(width > 0 && height > 0, "width > 0 and height > 0");
        return new Texture(width, height);
    }

    private Texture(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;

        final int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        mTextureId = textureIds[0];
        reset();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public void delete() {
        if (deleted) {
            return;
        }
        if (mTextureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
            deleted = true;
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

    public boolean isInvalid() {
        return deleted || !GLES20.glIsTexture(mTextureId);
    }

    public void reset() {
        if (mTextureId != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width(), height(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer) null);
        }
    }

    public void uploadBitmap(Bitmap bitmap) {
        if (mTextureId != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            if (bitmap != null && !bitmap.isRecycled()) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

    }
}
