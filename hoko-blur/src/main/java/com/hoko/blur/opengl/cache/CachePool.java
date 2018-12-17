package com.hoko.blur.opengl.cache;

import com.hoko.blur.util.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yuxfzju on 2017/1/21.
 */

public abstract class CachePool<K, V> {

    private static final int MAX_SIZE = 1024;

    private int mMaxSize;

    private List<V> mInternalCache;

    public CachePool() {
        this(MAX_SIZE);
    }

    public CachePool(int maxSize) {
        Preconditions.checkArgument(maxSize > 0, "maxSize <= 0");
        mMaxSize = maxSize;
        mInternalCache = new LinkedList<>();
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
            if (!mInternalCache.contains(v)) {
                synchronized (this) {
                    if (!mInternalCache.contains(v)) {
                        mInternalCache.add(v);
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
            Iterator<V> it = mInternalCache.iterator();
            while(it.hasNext()) {
                V value = it.next();
                if (checkHit(key, value)) {
                    it.remove();
                    previous = value;
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

    protected abstract boolean checkHit(K key, V value);

    private void trimToSize(int maxSize) {
        List<V> removedCollection = new ArrayList<>();
        synchronized (this) {
            while (mInternalCache.size() > maxSize && !mInternalCache.isEmpty()) {
                V removed = mInternalCache.remove(0);
                if (removed != null) {
                    removedCollection.add(removed);
                }
            }
        }

        for(V removed : removedCollection) {
            if (removed != null) {
                entryDeleted(removed);
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
