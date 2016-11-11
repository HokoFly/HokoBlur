package com.hoko.blurlibrary.functor;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by xiangpi on 16/11/9.
 */
public class DrawFunctor {

    private long mNativeFunctor;

    private Context mCtx;

    public DrawFunctor(Context context) {
        mCtx = context;
        mNativeFunctor = createNativeFunctor(new WeakReference<DrawFunctor>(this));
    }

    private static void postEventFromNative(WeakReference<DrawFunctor> functor, DrawFunctor.GLInfo info, int what) {
        Log.e("DrawFunctor", "---------------postEventFromNative----------------");

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
        Toast.makeText(mCtx, canvas.isHardwareAccelerated() + "---------------doDraw----------------" + canvas.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();

        if (canvas.isHardwareAccelerated()) {

            try {
                Class clazz = Class.forName("android.view.HardwareCanvas");

                Method callDrawGLFunctionMethod = clazz.getMethod("callDrawGLFunction", int.class);
                callDrawGLFunctionMethod.setAccessible(true);
                callDrawGLFunctionMethod.invoke(canvas, (int)mNativeFunctor);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    private void onInvoke(int what) {
        Log.d("DrawFunctor", "---------------onInvoke----------------");
//        Toast.makeText(mCtx, "---------------onInvoke----------------", Toast.LENGTH_SHORT).show();
        System.out.println("---------------onInvoke----------------");
    }

    private void onDraw(GLInfo info) {
        Log.d("DrawFunctor", "---------------onDraw----------------");
//        Toast.makeText(mCtx, "---------------onDraw----------------", Toast.LENGTH_SHORT).show();

        System.out.println("---------------onDraw----------------");

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
