package com.hoko.blurlibrary.processor;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.HokoBlur;
import com.hoko.blurlibrary.task.BlurSubTask;
import com.hoko.blurlibrary.task.BlurTaskManager;
import com.hoko.blurlibrary.filter.NativeBlurFilter;

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
        if (scaledInBitmap == null) {
            return null;
        }

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
