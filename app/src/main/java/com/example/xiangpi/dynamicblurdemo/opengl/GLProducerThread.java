package com.example.xiangpi.dynamicblurdemo.opengl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

/**
 * Created by xiangpi on 16/8/17.
 */
public class GLProducerThread extends Thread {

    interface GLRenderer {

        void onDrawFrame();
    }
    private boolean mRunDraw = true;

    private GLRenderer mGLRenderer;

    private SurfaceTexture mSurfaceTexture;

    private EGL10 mEgl;

    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;

    private EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;

    private EGLSurface mEGLSurface = EGL10.EGL_NO_SURFACE;

    private GL mGL;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private static final int EGL_OPENGL_ES2_BIT = 4;


    public GLProducerThread(GLRenderer glRenderer, SurfaceTexture surfaceTexture, boolean runDraw) {
        mGLRenderer = glRenderer;
        mSurfaceTexture = surfaceTexture;
        mRunDraw = runDraw;

    }

    private void initGL() {
        mEgl = (EGL10) EGLContext.getEGL();

        mEGLDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int []version = new int[2];

        mEgl.eglInitialize(mEGLDisplay, version);

        int []configAttribs = {
                EGL10.EGL_BUFFER_SIZE, 32,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
        };

        int []numConfigs = new int[1];

        EGLConfig []configs = new EGLConfig[1];

        mEgl.eglChooseConfig(mEGLDisplay, configAttribs, configs, 1, numConfigs);

        int []contextAttribs = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };

        mEGLContext = mEgl.eglCreateContext(mEGLDisplay, configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs);
        mEGLSurface = mEgl.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurfaceTexture, null);

        mEgl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);

        mGL = mEGLContext.getGL();


    }

    private void destoryGL() {
        mEgl.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEgl.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGLContext = EGL10.EGL_NO_CONTEXT;
        mEGLSurface = EGL10.EGL_NO_SURFACE;
    }

    @Override
    public void run() {
        initGL();

        ((GLRendererImpl)mGLRenderer).initGLRenderer();

        while(mRunDraw) {
            final long start = System.currentTimeMillis();
            mGLRenderer.onDrawFrame();
            mEgl.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            final long stop = System.currentTimeMillis();
            Log.d("opengl_glsurfaceview", ((float)(stop - start))  + "ms");
            try {
                sleep(5);
            } catch(InterruptedException e) {

            }
        }
        destoryGL();

    }
}
