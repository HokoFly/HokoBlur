package com.hoko.blur.opengl.texture;

import android.graphics.Bitmap;

import com.hoko.blur.util.Preconditions;

/**
 * Created by yuxfzju on 17/1/20.
 */

public class TextureFactory {
    public static Texture create(int width, int height) {
        Preconditions.checkArgument(width > 0 && height > 0, "width > 0 and height > 0");

        return new SimpleTexture(width, height);
    }

    public static Texture create(Bitmap bitmap) {
        Preconditions.checkNotNull(bitmap, "bitmap == null");
        Preconditions.checkArgument(!bitmap.isRecycled(), "bitmap is recycled");
        return new BitmapTexture(bitmap);
    }
}
