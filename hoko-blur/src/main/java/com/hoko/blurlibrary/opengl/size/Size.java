package com.hoko.blurlibrary.opengl.size;

import com.hoko.blurlibrary.api.ISize;

/**
 * Created by xiangpi on 2017/1/22.
 */

public final class Size implements ISize {

    private int mWidth;

    private int mHeight;

    public Size(int width, int height) {
        mWidth = width;

        mHeight = height;
    }

    public Size(Size size) {
        if (size == null) {
            throw new IllegalArgumentException("size is null");
        }

        mWidth = size.getWidth();

        mHeight = size.getHeight();
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof Size) {
            Size size = (Size) obj;
            return getWidth() == size.getWidth() && getHeight() == size.getHeight();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mHeight ^ ((mWidth << (Integer.SIZE / 2)) | (mWidth >>> (Integer.SIZE / 2)));
    }

    @Override
    public String toString() {
        return mWidth + "x" + mHeight;
    }
}
