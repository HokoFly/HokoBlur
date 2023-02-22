package com.hoko.blur.opengl.framebuffer;

import android.opengl.GLES20;

import com.hoko.blur.api.IFrameBuffer;
import com.hoko.blur.api.ITexture;

/**
 * Created by yuxfzju on 2017/1/22.
 */

class FrameBuffer implements IFrameBuffer {

    private int mFrameBufferId;

    private ITexture mTexture;

    FrameBuffer() {
        create();
    }

    FrameBuffer(int id) {
        mFrameBufferId = id;
    }

    public int id() {
        return mFrameBufferId;
    }

    public void id(int frameBufferId) {
        mFrameBufferId = frameBufferId;
    }

    @Override
    public void create() {
        final int[] frameBufferIds = new int[1];
        GLES20.glGenFramebuffers(1, frameBufferIds, 0);
        mFrameBufferId = frameBufferIds[0];
    }

    @Override
    public void bindTexture(ITexture texture) {
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

    public ITexture bindTexture() {
        return mTexture;
    }

    @Override
    public void bindSelf() {
        if (mFrameBufferId != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
        }
    }

    @Override
    public void delete() {
        if (mFrameBufferId != 0) {
            GLES20.glDeleteFramebuffers(1, new int[]{mFrameBufferId}, 0);
        }
    }
}
