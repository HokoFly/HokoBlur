package com.hoko.blur.opengl.texture;

import android.opengl.GLES20;

import com.hoko.blur.util.Preconditions;

import java.nio.Buffer;

/**
 * Created by yuxfzju on 17/1/20.
 */

class SimpleTexture extends Texture {

    SimpleTexture(int width, int height) {
        super(width, height);
        create();
    }

    @Override
    protected void onTextureCreated() {
        Preconditions.checkArgument(width() > 0 && height() > 0, "width > 0 and height > 0");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width(), height(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer) null);
    }

}
