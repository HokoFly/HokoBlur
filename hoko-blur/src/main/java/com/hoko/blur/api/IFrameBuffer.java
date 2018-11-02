package com.hoko.blur.api;

import com.hoko.blur.opengl.texture.Texture;

/**
 * Created by yuxfzju on 2017/1/22.
 */

public interface IFrameBuffer{
    int id();

    void id(int id);

    void bindTexture(Texture texture);

    Texture bindTexture();

    void bindSelf();

    void delete();
}
