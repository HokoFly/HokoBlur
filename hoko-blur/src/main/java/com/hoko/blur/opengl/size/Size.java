package com.hoko.blur.opengl.size;

import com.hoko.blur.api.ISize;

/**
 * Created by yuxfzju on 2017/1/22.
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

        mWidth = size.width();

        mHeight = size.height();
    }

    @Override
    public int width() {
        return mWidth;
    }

    @Override
    public void width(int mWidth) {
        this.mWidth = mWidth;
    }

    @Override
    public int height() {
        return mHeight;
    }

    @Override
    public void height(int mHeight) {
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
            return width() == size.width() && height() == size.height();
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
