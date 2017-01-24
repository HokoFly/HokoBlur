package com.hoko.blurlibrary.opengl.texture;

import android.graphics.Bitmap;

/**
 * Created by xiangpi on 17/1/20.
 */

public class TextureFactory {
    public static ITexture create(int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }

        return new SimpleTexture(width, height);
    }

    public static ITexture create(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        return new BitmapTexture(bitmap);
    }
}
