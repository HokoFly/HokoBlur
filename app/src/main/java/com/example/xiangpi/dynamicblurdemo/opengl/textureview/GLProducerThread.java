package com.example.xiangpi.dynamicblurdemo.opengl.textureview;

import android.graphics.SurfaceTexture;

import com.hoko.blurlibrary.opengl.offscreen.GLRenderer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by xiangpi on 16/8/17.
 */
public class GLProducerThread extends Thread {

    private boolean mRunDraw = true;

    private GLRenderer mGLRenderer;

    private SurfaceTexture mSurfaceTexture;

    private EGL10 mEgl;

    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;

    private EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;

    private EGLSurface mEGLSurface = EGL10.EGL_NO_SURFACE;

    private EGLConfig[] mEglConfigs;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private static final int EGL_OPENGL_ES2_BIT = 4;


    public GLProducerThread(GLRenderer glRenderer, SurfaceTexture surfaceTexture, boolean runDraw) {
        mGLRenderer = glRenderer;
        mSurfaceTexture = surfaceTexture;
        mRunDraw = runDraw;
        initGL();

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

        mEglConfigs = new EGLConfig[1];

        mEgl.eglChooseConfig(mEGLDisplay, configAttribs, mEglConfigs, 1, numConfigs);

        int []contextAttribs = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };

        mEGLContext = mEgl.eglCreateContext(mEGLDisplay, mEglConfigs[0], EGL10.EGL_NO_CONTEXT, contextAttribs);

        mEGLSurface = mEgl.eglCreateWindowSurface(mEGLDisplay, mEglConfigs[0], mSurfaceTexture, null);

    }

    private void destoryGL() {
        mEgl.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEgl.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGLContext = EGL10.EGL_NO_CONTEXT;
        mEGLSurface = EGL10.EGL_NO_SURFACE;
    }

    @Override
    public void run() {
        mEgl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);

        ((GLRendererImpl)mGLRenderer).initGLRenderer();

        while(mRunDraw) {

            mGLRenderer.onDrawFrame();
            mEgl.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            try {
                sleep(5);
            } catch(InterruptedException e) {

            }
        }
        destoryGL();

    }

    public void stopDraw() {
        mRunDraw = false;
    }

    public void startDraw() {
        mRunDraw = true;
    }
}
