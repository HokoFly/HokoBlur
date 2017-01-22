package com.hoko.blurlibrary.opengl.framebuffer;

import com.hoko.blurlibrary.opengl.cache.CachePool;
import com.hoko.blurlibrary.opengl.texture.ITexture;

/**
 * Created by xiangpi on 2017/1/22.
 */

public interface IFrameBuffer{
    int getId();

    void setId(int id);

    void bindTexture(ITexture texture);

    ITexture getBindTexture();

    void delete();
}
