package com.hoko.blur.opengl;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yuxfzju on 2025/7/10
 */
class TextureCache {
    private static final String TAG = "TextureCache";
    private static final int MAX_POOL_SIZE = 20;

    private static final TextureCache INSTANCE = new TextureCache();

    public static TextureCache getInstance() {
        return INSTANCE;
    }

    private final Map<String, TexturePool> sizePools = new ConcurrentHashMap<>();

    // 获取纹理
    public Texture acquireTexture(int width, int height) {
        String sizeKey = createSizeKey(width, height);
        TexturePool pool = sizePools.get(sizeKey);
        if (pool == null) {
            synchronized (sizePools) {
                pool = sizePools.get(sizeKey);
                if (pool == null) {
                    pool = new TexturePool(width, height);
                    sizePools.put(sizeKey, pool);
                }
            }
        }
        return pool.acquire();
    }

    public void releaseTexture(Texture texture) {
        if (texture == null) {
            return;
        }
        String sizeKey = createSizeKey(texture.width(), texture.height());
        TexturePool pool = sizePools.get(sizeKey);
        if (pool != null) {
            pool.release(texture);
        } else {
            texture.delete();
        }
    }

    private String createSizeKey(int width, int height) {
        return width + "x" + height;
    }

    public void clear() {
        synchronized (sizePools) {
            for (TexturePool pool : sizePools.values()) {
                pool.clear();
            }
            sizePools.clear();
        }
    }

    private static class TexturePool {
        private final int width;
        private final int height;
        private final Queue<Texture> availableTextures = new ArrayDeque<>();
        private final Set<Texture> inUseTextures = new HashSet<>();

        TexturePool(int width, int height) {
            this.width = width;
            this.height = height;
        }

        Texture acquire() {
            synchronized (this) {
                Texture texture = availableTextures.poll();
                if (texture == null) {
                    texture = Texture.create(width, height);
                } else {
                    texture.reset();
                }
                inUseTextures.add(texture);
                return texture;
            }
        }

        void release(Texture texture) {
            synchronized (this) {
                if (inUseTextures.contains(texture)) {
                    inUseTextures.remove(texture);
                    if (texture.isInvalid()) {
                        texture.delete();
                    } else {
                        availableTextures.offer(texture);
                        while (availableTextures.size() > MAX_POOL_SIZE) {
                            Texture oldest = availableTextures.poll();
                            if (oldest != null) {
                                oldest.delete();
                            }
                        }
                    }
                } else {
                    texture.delete();
                }
            }
        }

        void clear() {
            synchronized (this) {
                for (Texture texture : availableTextures) {
                    texture.delete();
                }
                availableTextures.clear();

                for (Texture texture : inUseTextures) {
                    texture.delete();
                }
                inUseTextures.clear();
            }
        }

    }
}
