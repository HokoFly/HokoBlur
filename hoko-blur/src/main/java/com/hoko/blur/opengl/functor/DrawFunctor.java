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
 * 对函数指针的封装，通过调用callDrawGLFunction，获取硬件绘制区域在屏幕的具体位置
 * Created by yuxfzju on 16/11/9.
 */
public class DrawFunctor {

    private long mNativeFunctor;

    private IRenderer<GLInfo> mBlurRenderer;

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

    public void doDraw(Canvas canvas) {
        if (canvas.isHardwareAccelerated()) {

            try {
                Class canvasClazz = null;
                Method callDrawGLFunctionMethod = null;

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

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void onInvoke(int what) {
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
        if (mNativeFunctor != 0) {
            releaseFunctor(mNativeFunctor);
        }
    }

    public IRenderer getRenderer() {
        return mBlurRenderer;
    }


    /**
     * 模糊区域与屏幕的相对位置信息
     */
    public static class GLInfo {
        public int clipLeft;
        public int clipTop;
        public int clipRight;
        public int clipBottom;
        public int viewportWidth;
        public int viewportHeight;
        public float[] transform;
        public boolean isLayer;

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
        System.loadLibrary("hoko_blur");
    }
}
