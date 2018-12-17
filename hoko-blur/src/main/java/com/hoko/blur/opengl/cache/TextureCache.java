package com.hoko.blur.opengl.cache;

import com.hoko.blur.api.ITexture;
import com.hoko.blur.opengl.texture.TextureFactory;
import com.hoko.blur.opengl.util.Size;

/**
 * Created by yuxfzju on 17/1/20.
 */

public class TextureCache {

    private static class TextureCacheHolder {
        private static final TextureCache INSTANCE = new TextureCache();
    }

    private CachePool<Size, ITexture> mCache;

    private TextureCache() {
        mCache = new CachePool<Size, ITexture>() {

            @Override
            protected ITexture create(Size size) {
                if (size == null) {
                    return null;
                }
                return TextureFactory.create(size.width(), size.height());
            }


            @Override
            protected void entryDeleted(ITexture texture) {
                if (texture != null) {
                    texture.delete();
                }
            }

            @Override
            protected boolean checkHit(Size size, ITexture texture) {
                return size != null && texture != null && size.width() == texture.width() && size.height() == texture.height();
            }
        };
    }

    public static TextureCache getInstance() {
        return TextureCacheHolder.INSTANCE;
    }

    public ITexture getTexture(int width, int height) {

        if (mCache != null) {
            return mCache.get(new Size(width, height));
        }

        return null;
    }

    public void recycleTexture(ITexture texture) {
        if (texture != null) {
            mCache.put(texture);
        }
    }

    public void deleteTextures() {
        if (mCache != null) {
            mCache.evictAll();
        }
    }
}
