package com.hoko.blurlibrary.opengl.functor;

import android.graphics.Canvas;
import android.opengl.Matrix;
import android.os.Build;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Created by xiangpi on 16/11/9.
 */
public class DrawFunctor {

    private long mNativeFunctor;

    private ScreenBlurRenderer mScreenBlurGenerator;

    public DrawFunctor() {
        mNativeFunctor = createNativeFunctor(new WeakReference<DrawFunctor>(this));
        mScreenBlurGenerator = new ScreenBlurRenderer();

    }

    private static void postEventFromNative(WeakReference<DrawFunctor> functor, DrawFunctor.GLInfo info, int what) {
        if(functor != null && functor.get() != null) {
            DrawFunctor d = (DrawFunctor)functor.get();
            if(info != null) {
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
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP){
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
                    callDrawGLFunctionMethod.invoke(canvas, (int)mNativeFunctor);
                }

//                mScreenBlurGenerator.initSourceBounds(0, 0, canvas.getWidth(), canvas.getHeight());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void onInvoke(int what) {
    }

    private void onDraw(final GLInfo info) {
//        Log.e("DrawFunctor", "bottom: " + info.clipBottom);
//        Log.e("DrawFunctor", "left: " + info.clipLeft);
//        Log.e("DrawFunctor", "right: " + info.clipRight);
//        Log.e("DrawFunctor", "top: " + info.clipTop);
//        Log.e("DrawFunctor", "viewportW: " + info.viewportWidth);
//        Log.e("DrawFunctor", "viewportH: " + info.viewportHeight);
//        Log.e("DrawFunctor", "transform[12]" + info.transform[12]);
//        Log.e("DrawFunctor", "transform[13]" + info.transform[13]);

        mScreenBlurGenerator.doBlur(info);

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
