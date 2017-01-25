package com.hoko.blurlibrary.opengl.cache;

import java.util.LinkedList;

/**
 * Created by xiangpi on 2017/1/21.
 */

public abstract class CachePool<K, T extends K> {

    private static final int MAX_SIZE = 1024;

    private int mMaxSize;

    private LinkedList<T> mList;

    public CachePool() {
        this(MAX_SIZE);
    }

    public CachePool(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        mMaxSize = maxSize;
        mList = new LinkedList<>();
    }

    public final T get(K key) {
        if (key == null) {
            throw new NullPointerException("size == null");
        }

        T listValue = remove(key);
        if (listValue != null) {
            return listValue;
        }

        //listValue is null
        return create(key);
    }

    public final void put(T t) {
        if (t == null) {
            throw new NullPointerException("value == null");
        }

        synchronized (this) {
            mList.add(t);
        }

        trimToSize(mMaxSize);


    }

    private T remove(K key) {
        if (key == null) {
            throw new NullPointerException("size == null");
        }

        T previous = null;
        synchronized (this) {
            for (T t : mList) {
//                if (t != null && t.getWidth() == size.getWidth() && t.getHeight() == size.getHeight()) {
//                    previous = mList.remove(mList.indexOf(t));
//                    break;
//                }
                if (checkHit(key, t)) {
                    previous = mList.remove(mList.indexOf(t));
                    break;
                }
            }
        }

        return previous;
    }

    public void delete(K key) {
        if (key == null) {
            throw new NullPointerException("size == null");
        }

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
