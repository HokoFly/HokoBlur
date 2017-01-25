package com.hoko.blurlibrary.api;

import com.hoko.blurlibrary.api.ISize;

/**
 * Created by xiangpi on 17/1/20.
 */

public interface ITexture extends ISize {

    int getId();

    void setId(int textureId);

    void delete();

}
