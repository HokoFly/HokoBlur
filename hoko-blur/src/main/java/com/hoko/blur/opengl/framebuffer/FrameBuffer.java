package com.hoko.blur.opengl.framebuffer;

import android.opengl.GLES20;

import com.hoko.blur.api.IFrameBuffer;
import com.hoko.blur.api.ITexture;
import com.hoko.blur.opengl.texture.Texture;
import com.hoko.blur.util.Preconditions;

/**
 * Created by yuxfzju on 2017/1/22.
 */

class FrameBuffer implements IFrameBuffer {

    private int mFrameBufferId;

    private Texture mTexture;

    FrameBuffer() {
        genFrameBuffer();
    }

    FrameBuffer(int id) {
        mFrameBufferId = id;
    }

    @Override
    public int id() {
        return mFrameBufferId;
    }

    @Override
    public void id(int frameBufferId) {
        mFrameBufferId = frameBufferId;
    }

    private void genFrameBuffer() {
        final int[] frameBufferIds = new int[1];

        GLES20.glGenFramebuffers(1, frameBufferIds, 0);

        mFrameBufferId = frameBufferIds[0];
    }

    @Override
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

    @Override
    public Texture bindTexture() {
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
