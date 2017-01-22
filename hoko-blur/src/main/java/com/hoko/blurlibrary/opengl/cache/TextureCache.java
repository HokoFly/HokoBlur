package com.hoko.blurlibrary.opengl.cache;

import android.graphics.Bitmap;
import android.util.SparseArray;

import com.hoko.blurlibrary.opengl.texture.BlurTexture;
import com.hoko.blurlibrary.opengl.texture.ISize;
import com.hoko.blurlibrary.opengl.texture.ITexture;
import com.hoko.blurlibrary.opengl.texture.TextureFactory;
import com.hoko.blurlibrary.util.Size;

/**
 * Created by xiangpi on 17/1/20.
 */

public class TextureCache {

    private static volatile TextureCache sInstance;

    private CachePool<ITexture> mTextures;

    private TextureCache() {
        mTextures = new CachePool<ITexture>() {
            @Override
            protected ITexture create(ISize size) {
                if (size == null) {
                    return null;
                }
                return TextureFactory.create(size.getWidth(), size.getHeight());
            }
        };
    }

    public static TextureCache getInstance() {
        if (sInstance == null) {
            synchronized (TextureCache.class) {
                if (sInstance == null) {
                    sInstance = new TextureCache();
                }
            }
        }

        return sInstance;
    }

    public ITexture getTexture(int width, int height) {

        if (mTextures != null) {
            return mTextures.get(new Size(width, height));
        }

        return null;
    }

    public ITexture getTexture(Bitmap bitmap) {
        // TODO: 2017/1/22
        return null;
    }

    public void recycleTexture(ITexture texture) {
        if (texture != null) {
            mTextures.put(texture);
        }
    }
}
