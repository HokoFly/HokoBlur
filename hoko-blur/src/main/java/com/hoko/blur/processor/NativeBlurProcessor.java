package com.hoko.blur.processor;

import android.graphics.Bitmap;
import android.util.Log;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.task.BlurSubTask;
import com.hoko.blur.task.BlurTaskManager;
import com.hoko.blur.filter.NativeBlurFilter;
import com.hoko.blur.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuxfzju on 16/9/7.
 */
class NativeBlurProcessor extends BlurProcessor {
    private static final String TAG = NativeBlurProcessor.class.getSimpleName();
    private static boolean LIB_LOADED;

    NativeBlurProcessor(Builder builder) {
        super(builder);
    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        Preconditions.checkNotNull(scaledInBitmap, "scaledInBitmap == null");

        if (!LIB_LOADED) {
            Log.e(TAG, "Native blur library is not loaded");
            return scaledInBitmap;
        }

        try {
            if (concurrent) {
                int cores = BlurTaskManager.getCores();
                List<BlurSubTask> hTasks = new ArrayList<>(cores);
                List<BlurSubTask> vTasks = new ArrayList<>(cores);

                for (int i = 0; i < cores; i++) {
                    hTasks.add(new BlurSubTask(HokoBlur.SCHEME_NATIVE, mMode, scaledInBitmap, mRadius, cores, i, HokoBlur.HORIZONTAL));
                    vTasks.add(new BlurSubTask(HokoBlur.SCHEME_NATIVE, mMode, scaledInBitmap, mRadius, cores, i, HokoBlur.VERTICAL));
                }

                BlurTaskManager.getInstance().invokeAll(hTasks);
                BlurTaskManager.getInstance().invokeAll(vTasks);
            } else {
                NativeBlurFilter.doFullBlur(mMode, scaledInBitmap, mRadius);
            }
        } catch (Throwable e) {
            Log.e(TAG, "Blur the bitmap error", e);
        }

        return scaledInBitmap;
    }


    static {
        try {
            System.loadLibrary("hoko_blur");
            LIB_LOADED = true;
        } catch (Throwable t) {
            LIB_LOADED = false;
            Log.e(TAG, "Failed to load the hoko blur native library", t);
        }
    }


}
