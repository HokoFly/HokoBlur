package com.hoko.blur.opengl.functor;

import android.graphics.Canvas;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;


import com.hoko.blur.api.IRenderer;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Java wrapper for the native functor.
 * The native functor is imported when call callDrawGLFunction().
 * Then the GLInfo about the drawing canvas will be available to locate the blur location.
 * Created by yuxfzju on 16/11/9.
 */
public class DrawFunctor {

    private static final String TAG = DrawFunctor.class.getSimpleName();

    private long mNativeFunctor;

    private IRenderer<GLInfo> mBlurRenderer;

    private static boolean LIB_LOADED;

    public DrawFunctor(IRenderer<GLInfo> blurRenderer) {
        mNativeFunctor = createNativeFunctor(new WeakReference<DrawFunctor>(this));
        mBlurRenderer = blurRenderer;

    }

    private static void postEventFromNative(WeakReference<DrawFunctor> functor, DrawFunctor.GLInfo info, int what) {
        if (functor != null && functor.get() != null) {
            DrawFunctor d = (DrawFunctor) functor.get();
            if (info != null) {
                d.onDraw(info);
            } else {
                d.onInvoke(what);
            }

        }
    }

    public boolean doDraw(Canvas canvas) {
        if (!LIB_LOADED) {
            Log.e(TAG, "Native blur library is not loaded, ");
            return false;
        }

        if (!canvas.isHardwareAccelerated()) {
            return false;
        }

        try {
            Class canvasClazz;
            Method callDrawGLFunctionMethod;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                canvasClazz = Class.forName("android.view.DisplayListCanvas");
                callDrawGLFunctionMethod = canvasClazz.getMethod("callDrawGLFunction2", long.class);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                canvasClazz = Class.forName("android.view.HardwareCanvas");
                callDrawGLFunctionMethod = canvasClazz.getMethod("callDrawGLFunction", long.class);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                canvasClazz = Class.forName("android.view.HardwareCanvas");
                callDrawGLFunctionMethod = canvasClazz.getMethod("callDrawGLFunction2", long.class);
            } else {
                canvasClazz = Class.forName("android.view.HardwareCanvas");
                callDrawGLFunctionMethod = canvasClazz.getMethod("callDrawGLFunction", int.class);
            }

            callDrawGLFunctionMethod.setAccessible(true);
            callDrawGLFunctionMethod.invoke(canvas, mNativeFunctor);
            return true;

        } catch (Throwable t) {
            Log.e(TAG, "canvas function [callDrawGLFunction()] error", t);
            return false;
        }
    }

    private void onInvoke(int what) {
        Log.w(TAG, "Cannot get the GLInfo, code=" + what);
    }

    private void onDraw(final GLInfo info) {
//        Log.i("DrawFunctor", "onDraw: " + info);
        if (mBlurRenderer != null) {
            // FIX: transform computation caused by the margin
            if (info.transform[12] < info.clipLeft) {
                info.transform[12] = info.clipLeft;
            }
            if (info.transform[13] < info.clipTop) {
                info.transform[13] = info.clipTop;
            }
            mBlurRenderer.onDrawFrame(info);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            if (mNativeFunctor != 0) {
                releaseFunctor(mNativeFunctor);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Release functor error", t);
        }

    }


    /**
     * The blur location on the screen
     */
    public static class GLInfo {
        int clipLeft;
        int clipTop;
        int clipRight;
        int clipBottom;
        int viewportWidth;
        int viewportHeight;
        float[] transform;
        boolean isLayer;

        public GLInfo() {
            this.transform = new float[16];
            Matrix.setIdentityM(this.transform, 0);
        }

        public GLInfo(int width, int height) {
            this.viewportWidth = width;
            this.viewportHeight = height;
        }

        @Override
        public String toString() {
            return "GLInfo{" +
                    "clipLeft=" + clipLeft +
                    ", clipTop=" + clipTop +
                    ", clipRight=" + clipRight +
                    ", clipBottom=" + clipBottom +
                    ", viewportWidth=" + viewportWidth +
                    ", viewportHeight=" + viewportHeight +
                    ", transform=" + Arrays.toString(transform) +
                    ", isLayer=" + isLayer +
                    '}';
        }
    }

    public native long createNativeFunctor(WeakReference<DrawFunctor> functor);

    private native void releaseFunctor(long functorPtr);

    static {
        try {
            System.loadLibrary("hoko_blur");
            LIB_LOADED = true;
        } catch (Throwable t) {
            LIB_LOADED = false;
            Log.e(TAG, "Failed to load hoko blur library", t);
        }
    }
}
