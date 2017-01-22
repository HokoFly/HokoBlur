package com.hoko.blurlibrary.opengl.texture;

import com.hoko.blurlibrary.opengl.size.ISize;

/**
 * Created by xiangpi on 17/1/20.
 */

public interface ITexture extends ISize {

    int getId();

    void setId(int textureId);

    void delete();

}
