package com.hoko.blur.processor;

import android.graphics.Bitmap;

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

    NativeBlurProcessor(Builder builder) {
        super(builder);
    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        Preconditions.checkNotNull(scaledInBitmap, "scaledInBitmap == null");

        if (concurrent) {
            try {
                int cores = BlurTaskManager.getCores();
                List<BlurSubTask> hTasks = new ArrayList<BlurSubTask>(cores);
                List<BlurSubTask> vTasks = new ArrayList<BlurSubTask>(cores);

                for (int i = 0; i < cores; i++) {
                    hTasks.add(new BlurSubTask(HokoBlur.SCHEME_NATIVE, mMode, scaledInBitmap, mRadius, cores, i, HokoBlur.HORIZONTAL));
                    vTasks.add(new BlurSubTask(HokoBlur.SCHEME_NATIVE, mMode, scaledInBitmap, mRadius, cores, i, HokoBlur.VERTICAL));
                }

                BlurTaskManager.getInstance().invokeAll(hTasks);
                BlurTaskManager.getInstance().invokeAll(vTasks);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            NativeBlurFilter.doFullBlur(mMode, scaledInBitmap, mRadius);
        }


        return scaledInBitmap;
    }

}
