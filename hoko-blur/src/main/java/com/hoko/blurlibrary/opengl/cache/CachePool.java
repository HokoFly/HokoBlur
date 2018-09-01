package com.hoko.blurlibrary.opengl.cache;

import com.hoko.blurlibrary.util.Preconditions;

import java.util.LinkedList;

/**
 * Created by yuxfzju on 2017/1/21.
 */

public abstract class CachePool<K, T extends K> {

    private static final int MAX_SIZE = 1024;

    private int mMaxSize;

    private LinkedList<T> mList;

    public CachePool() {
        this(MAX_SIZE);
    }

    public CachePool(int maxSize) {
        Preconditions.checkArgument(maxSize > 0, "maxSize <= 0");
        mMaxSize = maxSize;
        mList = new LinkedList<>();
    }

    public final T get(K key) {
        Preconditions.checkNotNull(key, "size == null");
        T listValue = remove(key);
        if (listValue != null) {
            return listValue;
        }

        //listValue is null
        return create(key);
    }

    public final void put(T t) {
        Preconditions.checkNotNull(t, "value == null");
        synchronized (this) {
            mList.add(t);
        }

        trimToSize(mMaxSize);


    }

    private T remove(K key) {
        Preconditions.checkNotNull(key, "key == null");

        T previous = null;
        synchronized (this) {
            for (T t : mList) {
                if (checkHit(key, t)) {
                    previous = mList.remove(mList.indexOf(t));
                    break;
                }
            }
        }

        return previous;
    }

    public void delete(K key) {
        Preconditions.checkNotNull(key, "key == null");
        T removed = remove(key);
        if (removed != null) {
            entryDeleted(removed);
        }
    }

    protected T create(K key) {
        return null;
    }

    protected void entryDeleted(T t) {

    }

    protected abstract boolean checkHit(K a, T b);

    private void trimToSize(int maxSize) {
        while(true) {
            synchronized (this) {
                if (mList.size() <= maxSize || mList.isEmpty()) {
                    break;
                }

                T removed = mList.removeFirst();
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
