package com.hoko.blur.opengl;

/**
 * Created by yuxfzju on 2017/1/21.
 */

class FrameBufferCache {

    private static class FrameBufferCacheHolder {
        private static final FrameBufferCache INSTANCE = new FrameBufferCache();
    }

    private final CachePool<Object, FrameBuffer> mCache;

    private FrameBufferCache() {
        mCache = new CachePool<>(32) {
            @Override
            protected FrameBuffer create(Object key) {
                return FrameBuffer.create();
            }

            @Override
            protected boolean checkHit(Object key, FrameBuffer frameBuffer) {
                return true;
            }

            @Override
            protected void entryDeleted(FrameBuffer frameBuffer) {
                if (frameBuffer != null) {
                    frameBuffer.delete();
                }
            }
        };

    }

    public static FrameBufferCache getInstance() {
        return FrameBufferCacheHolder.INSTANCE;
    }

    public FrameBuffer getFrameBuffer() {
        if (mCache != null) {
            return mCache.get(new Object());
        }
        return null;
    }

    public void recycleFrameBuffer(FrameBuffer frameBuffer) {
        if (frameBuffer != null) {
            mCache.put(frameBuffer);
        }
    }

    public void deleteFrameBuffers() {
        if (mCache != null) {
            mCache.evictAll();
        }
    }

}
