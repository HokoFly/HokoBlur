package com.hoko.blur.opengl;

import android.opengl.GLES20;

/**
 * Created by yuxfzju on 2017/1/22.
 */

class FrameBuffer {

    private int mFrameBufferId;

    private Texture mTexture;

    public static FrameBuffer create() {
        return new FrameBuffer();
    }

    public static FrameBuffer create(int id) {
        return new FrameBuffer(id);
    }

    private FrameBuffer() {
        final int[] frameBufferIds = new int[1];
        GLES20.glGenFramebuffers(1, frameBufferIds, 0);
        mFrameBufferId = frameBufferIds[0];
    }

    private FrameBuffer(int id) {
        mFrameBufferId = id;
    }

    public int id() {
        return mFrameBufferId;
    }

    public void id(int frameBufferId) {
        mFrameBufferId = frameBufferId;
    }


    public void bindTexture(Texture texture) {
        if (texture == null) {
            return;
        }
        mTexture = texture;
        if (texture.id() != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, texture.id(), 0);
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public Texture bindTexture() {
        return mTexture;
    }

    public void bindSelf() {
        if (mFrameBufferId != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
        }
    }

    public void delete() {
        if (mFrameBufferId != 0) {
            GLES20.glDeleteFramebuffers(1, new int[]{mFrameBufferId}, 0);
        }
    }
}
