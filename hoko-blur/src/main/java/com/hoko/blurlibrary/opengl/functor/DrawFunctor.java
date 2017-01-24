package com.hoko.blurlibrary.opengl.functor;

import android.graphics.Canvas;
import android.opengl.Matrix;
import android.os.Build;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.generator.IBlur;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Created by xiangpi on 16/11/9.
 */
public class DrawFunctor implements IBlur {

    private long mNativeFunctor;

    private ScreenBlurRenderer mBlurRenderer;

    public DrawFunctor() {
        mNativeFunctor = createNativeFunctor(new WeakReference<DrawFunctor>(this));
        mBlurRenderer = new ScreenBlurRenderer();

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
//        Log.e("DrawFunctor", " left: " + info.clipLeft + " bottom: " + info.clipBottom + " right: " + info.clipRight + " top: " + info.clipTop);
        mBlurRenderer.doBlur(info);
    }

    public void destroy() {
        mBlurRenderer.free();
    }

    @Override
    public void setBlurMode(@Blur.BlurMode int mode) {
        mBlurRenderer.setBlurMode(mode);
    }

    @Override
    public void setBlurRadius(int radius) {
        mBlurRenderer.setBlurRadius(radius);
    }

    @Override
    public void setSampleFactor(float factor) {
        mBlurRenderer.setSampleFactor(factor);
    }

    @Override
    public int getBlurMode() {
        return mBlurRenderer.getBlurMode();
    }

    @Override
    public int getBlurRadius() {
        return mBlurRenderer.getBlurRadius();
    }

    @Override
    public float getSampleFactor() {
        return mBlurRenderer.getSampleFactor();
    }

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
    }

    public native long createNativeFunctor(WeakReference<DrawFunctor> functor);

    static {
        System.loadLibrary("ImageBlur");
    }
}
