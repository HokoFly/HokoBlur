package com.hoko.blurlibrary.api;

/**
 * Created by yuxfzju on 2017/1/22.
 */

public interface IFrameBuffer{
    int id();

    void id(int id);

    void bindTexture(ITexture texture);

    ITexture bindTexture();

    void bindSelf();

    void delete();
}
