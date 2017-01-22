package com.hoko.blurlibrary.opengl.cache;

import com.hoko.blurlibrary.opengl.texture.ISize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by xiangpi on 2017/1/21.
 */

public class CachePool<T extends ISize> {

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

    public final T get(ISize size) {
        if (size == null) {
            throw new NullPointerException("size == null");
        }

        T listValue = remove(size);
        if (listValue != null) {
            return listValue;
        }

        //listValue is null
        return create(size);
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

    public final T remove(ISize size) {
        if (size == null) {
            throw new NullPointerException("size == null");
        }

        T previous = null;
        synchronized (this) {
            for (T t : mList) {
                if (t != null && t.getWidth() == size.getWidth() && t.getHeight() == size.getHeight()) {
                    previous = mList.remove(mList.indexOf(t));
                    break;
                }
            }
        }

        return previous;
    }

    protected T create(ISize size) {
        return null;
    }

    private void trimToSize(int maxSize) {
        while(true) {
            synchronized (this) {
                if (mList.size() <= maxSize || mList.isEmpty()) {
                    break;
                }

                mList.removeFirst();
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
