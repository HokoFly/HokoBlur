package com.hoko.blurlibrary.opengl.framebuffer;

import android.opengl.GLES20;

import com.hoko.blurlibrary.opengl.texture.ITexture;

/**
 * Created by xiangpi on 2017/1/22.
 */

class FrameBuffer implements IFrameBuffer {

    private int mFrameBufferId;

    private ITexture mTexture;

    public FrameBuffer() {
        genFrameBuffer();
    }

    public FrameBuffer(int id) {
        mFrameBufferId = id;
    }

    @Override
    public int getId() {
        return mFrameBufferId;
    }

    @Override
    public void setId(int frameBufferId) {
        mFrameBufferId = frameBufferId;
    }

    private void genFrameBuffer() {
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

        if (texture.getId() != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, texture.getId(), 0);
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public ITexture getBindTexture() {
        return mTexture;
    }

    @Override
    public void bindSelf() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
    }

    @Override
    public void delete() {
        GLES20.glDeleteFramebuffers(1, new int[]{mFrameBufferId}, 0);

    }
}
