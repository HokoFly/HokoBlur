package com.xiangpi.blurlibrary.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static javax.microedition.khronos.opengles.GL10.GL_RGBA;
import static javax.microedition.khronos.opengles.GL10.GL_UNSIGNED_BYTE;

/**
 * Created by xiangpi on 16/8/29.
 */
public class OffScreenBuffer {

    private EGL10 mEgl;

    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;

    private EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;

    private EGLSurface mEGLSurface = EGL10.EGL_NO_SURFACE;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private static final int EGL_OPENGL_ES2_BIT = 4;

    private EGLConfig[] mEglConfigs = new EGLConfig[1];
    private int[] mContextAttribs;
    private int mWidth;
    private int mHeight;

    private GLRenderer mRenderer;

    private Bitmap mOutputBitmap;

    public OffScreenBuffer() {
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
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
        };

        int []numConfigs = new int[1];

        mEgl.eglChooseConfig(mEGLDisplay, configAttribs, mEglConfigs, 1, numConfigs);

        mContextAttribs = new int[] {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };

        mEGLContext = mEgl.eglCreateContext(mEGLDisplay, mEglConfigs[0], EGL10.EGL_NO_CONTEXT, mContextAttribs);

    }

    private void initSurface() {
        int[] surfaceAttribs = {
                EGL10.EGL_WIDTH, mWidth,
                EGL10.EGL_HEIGHT, mHeight,
                EGL10.EGL_NONE
        };

        mEGLSurface = mEgl.eglCreatePbufferSurface(mEGLDisplay, mEglConfigs[0], surfaceAttribs);

        mEgl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);

    }

    public void setRenderer(GLRenderer renderer) {
        mRenderer = renderer;
        mWidth = mRenderer.getInputBitmap().getWidth();
        mHeight = mRenderer.getInputBitmap().getHeight();

        initSurface();

        if (mRenderer != null) {
            mRenderer.onSurfaceCreated();
            mRenderer.onSurfaceChanged(mWidth, mHeight);
        }
    }

    public Bitmap getBitmap() {
        if (mRenderer != null) {
            mRenderer.onDrawFrame();
            mEgl.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        }
        convertToBitmap();
        unbindEglCurrent();
        return mOutputBitmap;

    }

    private void convertToBitmap() {
        int[] iat = new int[mWidth * mHeight];
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, ib);
        int[] ia = ib.array();

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
//        for (int i = 0; i < mHeight; i++) {
//            for (int j = 0; j < mWidth; j++) {
//                iat[(mHeight - i - 1) * mWidth + j] = ia[i * mWidth + j];
//            }
//        }

        mOutputBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mOutputBitmap.copyPixelsFromBuffer(IntBuffer.wrap(ia));
    }

    /**
     * 在当前线程结束一系列渲染和像素读取操作之后，需要将EGLContext与当前线程解绑，
     * 这样才能在下次模糊操作的另一个线程中，继续使用当前EGLContext，达到共享EGLContext的目的。
     * 当前线程绑定EGLContext，只需调用eglMakeCurrent()
     */
    private void unbindEglCurrent() {
        mEgl.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);

    }
}
