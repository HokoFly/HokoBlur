package com.hoko.blurlibrary.task;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.HokoBlur;
import com.hoko.blurlibrary.anno.Direction;
import com.hoko.blurlibrary.anno.Mode;
import com.hoko.blurlibrary.anno.Scheme;
import com.hoko.blurlibrary.filter.OriginBlurFilter;
import com.hoko.blurlibrary.filter.NativeBlurFilter;

import java.util.concurrent.Callable;

/**
 * 对bitmap的模糊任务，任务主要模糊bitmap的一部分，通过线程池管理，实现对bitmap的并发模糊处理
 * Created by yuxfzju on 2017/2/17.
 */

public class BlurSubTask implements Callable<Void> {

    @Scheme
    private final int mScheme;
    @Mode
    private final int mMode;
    private final Bitmap mBitmapOut;
    private final int mRadius;
    private final int mIndex;
    private final int mCores;

    @Direction
    private final int mDirection;

    public BlurSubTask(@Scheme int scheme, @Mode int mode, Bitmap bitmapOut, int radius, int cores, int index, @Direction int direction) {
        mScheme = scheme;
        mMode = mode;
        mBitmapOut = bitmapOut;
        mRadius = radius;
        mIndex = index;
        mCores = cores;
        mDirection = direction;
    }

    @Override
    public Void call() throws Exception {
        if (mBitmapOut == null || mBitmapOut.isRecycled() || mCores <= 0) {
            return null;
        }

        applyPixelsBlur();

        return null;
    }

    private void applyPixelsBlur() {
        switch (mScheme) {
            case HokoBlur.SCHEME_NATIVE:
                NativeBlurFilter.doBlur(mMode, mBitmapOut, mRadius, mCores, mIndex, mDirection);
                break;

            case HokoBlur.SCHEME_JAVA:
                OriginBlurFilter.doBlur(mMode, mBitmapOut, mRadius, mCores, mIndex, mDirection);
                break;

            case HokoBlur.SCHEME_OPENGL:
                //暂时不支持并行执行
                break;
            case HokoBlur.SCHEME_RENDER_SCRIPT:
                //RenderScript本身为并行处理
                break;
            default:
                break;

        }

    }
}
