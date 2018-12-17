package com.hoko.blur.opengl.cache;

import com.hoko.blur.api.IFrameBuffer;
import com.hoko.blur.opengl.framebuffer.FrameBufferFactory;

/**
 * Created by yuxfzju on 2017/1/21.
 */

public class FrameBufferCache {

    private static class FrameBufferCacheHolder {
        private static final FrameBufferCache INSTANCE = new FrameBufferCache();
    }

    private CachePool<Object, IFrameBuffer> mCache;

    private volatile IFrameBuffer sDisplayFrameBuffer;

    private FrameBufferCache() {
        mCache = new CachePool<Object, IFrameBuffer>() {
            @Override
            protected IFrameBuffer create(Object key) {
                return FrameBufferFactory.create();
            }

            @Override
            protected boolean checkHit(Object key, IFrameBuffer frameBuffer) {
                return true;
            }

            @Override
            protected void entryDeleted(IFrameBuffer frameBuffer) {
                if (frameBuffer != null) {
                    frameBuffer.delete();
                }
            }
        };

    }

    public static FrameBufferCache getInstance() {
        return FrameBufferCacheHolder.INSTANCE;
    }

    public IFrameBuffer getFrameBuffer() {
        if (mCache != null) {
            return mCache.get(new Object());
        }
        return null;
    }

    public IFrameBuffer getDisplayFrameBuffer() {
        if (sDisplayFrameBuffer == null) {
            synchronized (this) {
                if (sDisplayFrameBuffer == null) {
                    sDisplayFrameBuffer = FrameBufferFactory.getDisplayFrameBuffer();
                }
            }
        }

        return sDisplayFrameBuffer;
    }

    public void recycleFrameBuffer(IFrameBuffer frameBuffer) {
        if (frameBuffer != null) {
            mCache.put(frameBuffer);
        }
    }

    public void deleteFrameBuffers() {
        if (mCache != null) {
            mCache.evictAll();
        }

        synchronized (this) {
            if (sDisplayFrameBuffer != null) {
                sDisplayFrameBuffer.delete();
                sDisplayFrameBuffer = null;
            }
        }
    }

}
