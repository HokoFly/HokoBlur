package com.hoko.blurlibrary.opengl.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by xiangpi on 2017/1/21.
 */

public class CachePool<K, V> {

    private static final int MAX_VALUE = 1024;

    private int mMaxSize;

    private int mPoolSize;

    private LinkedHashMap<K, V> mMap;

    public CachePool() {
        this(MAX_VALUE);
    }

    public CachePool(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        mMaxSize = maxSize;
        mMap = new LinkedHashMap<>(0, 0.75f, true);
    }

    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V mapValue;
        synchronized (this) {
            mapValue = mMap.get(key);

            if (mapValue != null) {
                mMap.remove(key);

                return mapValue;
            }

        }

        //mapValue is null
        V createdValue = create(key);

        //create太久，再check一次
        synchronized (this) {
            mapValue = mMap.get(key);

            if (mapValue != null) {
                mMap.remove(key);
                return mapValue;
            } else {
                return createdValue;
            }
        }
    }

    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        V previous = null;

        synchronized (this) {
            previous = mMap.put(key, value);
        }

        trimToSize(mMaxSize);

        return previous;


    }

    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V previous = null;
        synchronized (this) {
            previous = mMap.remove(key);
        }

        return previous;
    }

    protected V create(K key) {
        return null;
    }

    private void trimToSize(int maxSize) {
        while(true) {
            K key;
            synchronized (this) {
                if (mMap.size() <= maxSize || mMap.isEmpty()) {
                    break;
                }

                Map.Entry<K, V> entry = mMap.entrySet().iterator().next();
                key = entry.getKey();
                mMap.remove(key);
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
