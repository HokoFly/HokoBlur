package com.hoko.blur.opengl.cache;

import com.hoko.blur.util.Preconditions;

import java.util.LinkedList;

/**
 * Created by yuxfzju on 2017/1/21.
 */

public abstract class CachePool<K, V> {

    private static final int MAX_SIZE = 1024;

    private int mMaxSize;

    private LinkedList<V> mList;

    public CachePool() {
        this(MAX_SIZE);
    }

    public CachePool(int maxSize) {
        Preconditions.checkArgument(maxSize > 0, "maxSize <= 0");
        mMaxSize = maxSize;
        mList = new LinkedList<>();
    }

    public final V get(K key) {
        Preconditions.checkNotNull(key, "size == null");
        V listValue = remove(key);
        if (listValue != null) {
            return listValue;
        }

        //listValue is null
        return create(key);
    }

    public final void put(V v) {
        Preconditions.checkNotNull(v, "value == null");
        try {
            if (!mList.contains(v)) {
                synchronized (this) {
                    if (!mList.contains(v)) {
                        mList.add(v);
                    }
                }
            }
        } finally {
            trimToSize(mMaxSize);
        }

    }

    private V remove(K key) {
        Preconditions.checkNotNull(key, "key == null");

        V previous = null;
        synchronized (this) {
            for (V v : mList) {
                if (checkHit(key, v)) {
                    previous = mList.remove(mList.indexOf(v));
                    break;
                }
            }
        }

        return previous;
    }

    public void delete(K key) {
        Preconditions.checkNotNull(key, "key == null");
        V removed = remove(key);
        if (removed != null) {
            entryDeleted(removed);
        }
    }

    protected V create(K key) {
        return null;
    }

    protected void entryDeleted(V v) {

    }

    protected abstract boolean checkHit(K a, V b);

    private void trimToSize(int maxSize) {
        while (true) {
            synchronized (this) {
                if (mList.size() <= maxSize || mList.isEmpty()) {
                    break;
                }

                V removed = mList.removeFirst();
                if (removed != null) {
                    entryDeleted(removed);
                }

            }
        }
    }

    public synchronized final int maxSize() {
        return mMaxSize;
    }

    public final void evictAll() {
        trimToSize(-1);
    }

}
