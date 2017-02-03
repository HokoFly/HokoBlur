package com.hoko.blurlibrary.api;

/**
 * Created by xiangpi on 2017/1/22.
 */

public interface IFrameBuffer{
    int getId();

    void setId(int id);

    void bindTexture(ITexture texture);

    ITexture getBindTexture();

    void bindSelf();

    void delete();
}
