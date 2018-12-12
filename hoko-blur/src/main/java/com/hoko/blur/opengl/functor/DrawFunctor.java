package com.hoko.blur.opengl.functor;

import android.graphics.Canvas;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

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

    public interface DrawLocationObserver {
        void onLocated(GLInfo info, boolean isChild);

        void onLocateError(int what);
    }

    private final long mNativeFunctor;

    private final DrawLocationObserver mObserver;

    private GLInfo mParentGLInfo;

    private static boolean LIB_LOADED;

    public DrawFunctor(DrawLocationObserver observer) {
        mNativeFunctor = createNativeFunctor(new WeakReference<DrawFunctor>(this));
        mObserver = observer;

    }

    private static void postEventFromNative(WeakReference<DrawFunctor> functor, DrawFunctor.GLInfo info, int what) {
        if (functor != null && functor.get() != null) {
            DrawFunctor d = functor.get();
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
                callDrawGLFunctionMethod.setAccessible(true);
                callDrawGLFunctionMethod.invoke(canvas, mNativeFunctor);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                canvasClazz = Class.forName("android.view.HardwareCanvas");
                callDrawGLFunctionMethod = canvasClazz.getMethod("callDrawGLFunction", long.class);
                callDrawGLFunctionMethod.setAccessible(true);
                callDrawGLFunctionMethod.invoke(canvas, mNativeFunctor);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                canvasClazz = Class.forName("android.view.HardwareCanvas");
                callDrawGLFunctionMethod = canvasClazz.getMethod("callDrawGLFunction2", long.class);
                callDrawGLFunctionMethod.setAccessible(true);
                callDrawGLFunctionMethod.invoke(canvas, mNativeFunctor);
            } else {
                canvasClazz = Class.forName("android.view.HardwareCanvas");
                callDrawGLFunctionMethod = canvasClazz.getMethod("callDrawGLFunction", int.class);
                callDrawGLFunctionMethod.setAccessible(true);
                callDrawGLFunctionMethod.invoke(canvas, (int) mNativeFunctor);
            }

            return true;

        } catch (Throwable t) {
            Log.e(TAG, "canvas function [callDrawGLFunction()] error", t);
            return false;
        }
    }

    private void onInvoke(int what) {
        if (mObserver != null) {
            mObserver.onLocateError(what);
        }
        Log.w(TAG, "Cannot get the GLInfo, code=" + what);
    }

    private void onDraw(final GLInfo info) {
//        Log.i("DrawFunctor", "onDraw: " + info);
        boolean isChildRedraw;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT
                && mParentGLInfo != null && mParentGLInfo.contains(info)) {
            isChildRedraw = true;
        } else {
            mParentGLInfo = info;
            isChildRedraw = false;
        }

        if (mObserver != null) {
            mObserver.onLocated(mParentGLInfo, isChildRedraw);
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

        public boolean contains(GLInfo info) {
            if (this.clipLeft == info.clipLeft && this.clipRight == info.clipRight
                    && this.clipTop == info.clipTop && this.clipBottom == info.clipBottom) {
                return false;
            }

            return this.clipLeft < this.clipRight && this.clipTop < this.clipBottom
                    && this.clipLeft <= info.clipLeft && this.clipTop <= info.clipTop
                    && this.clipRight >= info.clipRight && this.clipBottom >= info.clipBottom;
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
