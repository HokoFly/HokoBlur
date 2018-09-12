package com.hoko.blur.opengl.framebuffer;

import android.opengl.GLES20;

import com.hoko.blur.api.IFrameBuffer;

/**
 * Created by yuxfzju on 2017/1/22.
 */

public class FrameBufferFactory {

    public static IFrameBuffer create() {
        return new FrameBuffer();
    }

    public static IFrameBuffer create(int id) {
        return new FrameBuffer(id);
    }

    public static IFrameBuffer getDisplayFrameBuffer() {
        // 获得当前绑定的FBO（屏上）
        final int[] displayFbo = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, displayFbo, 0);
        return create(displayFbo[0]);
    }
}
