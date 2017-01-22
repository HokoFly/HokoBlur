package com.hoko.blurlibrary.opengl.texture;

import android.graphics.Bitmap;

/**
 * Created by xiangpi on 17/1/20.
 */

public interface ITexture {

    void setId(int textureId);

    int getId();

    void setWidth(int width);

    int getWidth();

    void setHeight(int height);

    int getHeight();
}
