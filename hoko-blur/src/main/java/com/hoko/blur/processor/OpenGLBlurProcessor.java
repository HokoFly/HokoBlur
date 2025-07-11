package com.hoko.blur.processor;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.hoko.blur.opengl.EglBuffer;
import com.hoko.blur.task.AsyncBlurTask;
import com.hoko.blur.task.BitmapAsyncBlurTask;
import com.hoko.blur.task.BlurTaskManager;
import com.hoko.blur.task.ViewAsyncBlurTask;
import com.hoko.blur.util.Preconditions;

import java.util.concurrent.Future;


/**
 * Created by yuxfzju on 16/9/7.
 */
class OpenGLBlurProcessor extends BlurProcessor {
    private static final String TAG = OpenGLBlurProcessor.class.getSimpleName();
    private final EglBuffer mEglBuffer = new EglBuffer();

    OpenGLBlurProcessor(HokoBlurBuild builder) {
        super(builder);
    }

    @Override
    protected Bitmap doInnerBlur(Bitmap scaledInBitmap, boolean concurrent) {
        Preconditions.checkNotNull(scaledInBitmap, "scaledInBitmap == null");
        Preconditions.checkArgument(!scaledInBitmap.isRecycled(), "You must input an unrecycled bitmap !");
        try {
            return mEglBuffer.getBlurBitmap(scaledInBitmap, mRadius, mMode);
        } catch (Throwable e) {
            Log.e(TAG, "Blur the bitmap error", e);
        }
        return scaledInBitmap;
    }

    @Override
    public Future<?> asyncBlur(Bitmap bitmap, AsyncBlurTask.Callback callback) {
        return BlurTaskManager.getInstance().enqueue(new BitmapAsyncBlurTask(this, bitmap, callback, mDispatcher));
    }

    @Override
    public Future<?> asyncBlur(View view, AsyncBlurTask.Callback callback) {
        return BlurTaskManager.getInstance().enqueue(new ViewAsyncBlurTask(this, view, callback, mDispatcher));
    }

}