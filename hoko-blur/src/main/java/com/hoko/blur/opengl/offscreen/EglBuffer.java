package com.hoko.blur.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.hoko.blur.anno.Mode;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static javax.microedition.khronos.opengles.GL10.GL_RGBA;
import static javax.microedition.khronos.opengles.GL10.GL_UNSIGNED_BYTE;

/**
 * Created by yuxfzju on 16/8/29.
 */
public class EglBuffer {
    private static final String TAG = EglBuffer.class.getSimpleName();

    private EGL10 mEgl;

    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;

    private EGLSurface mEGLSurface;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private static final int EGL_OPENGL_ES2_BIT = 4;

    private EGLConfig[] mEglConfigs = new EGLConfig[1];
    private int[] mContextAttrs = new int[]{
            EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
    };


    //EGLContext、EGLSurface and Renderer are bound to current thread.
    // So here use the ThreadLocal to implement Thread isolation.
    private ThreadLocal<OffScreenBlurRenderer> mThreadRenderer = new ThreadLocal<>();

    private ThreadLocal<EGLContext> mThreadEGLContext = new ThreadLocal<>();

    public EglBuffer() {
        initGL();
    }

    private void initGL() {

        int[] configAttribs = {
                EGL10.EGL_BUFFER_SIZE, 32,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
        };

        mEgl = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        mEgl.eglInitialize(mEGLDisplay, new int[2]);
        mEgl.eglChooseConfig(mEGLDisplay, configAttribs, mEglConfigs, 1, new int[1]);
    }

    private EGLSurface createSurface(int width, int height) {
        int[] surfaceAttrs = {
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL10.EGL_NONE
        };

        EGLSurface eglSurface = mEgl.eglCreatePbufferSurface(mEGLDisplay, mEglConfigs[0], surfaceAttrs);

        mEgl.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, getEGLContext());

        return eglSurface;

    }


    public Bitmap getBlurBitmap(Bitmap bitmap) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();

        try {
            mEGLSurface = createSurface(w, h);
            if (mEGLSurface == null) {
                Log.e(TAG, "Create surface error");
                return bitmap;
            }

            OffScreenBlurRenderer renderer = getRenderer();
            if (renderer != null) {
                renderer.onDrawFrame(bitmap);
                mEgl.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            } else {
                Log.e(TAG, "Renderer is unavailable");
                return bitmap;
            }
            convertToBitmap(bitmap);
        } catch (Throwable t) {
            Log.e(TAG, "Blur the bitmap error", t);
        } finally {
            destroyEglSurface();
        }

        return bitmap;

    }

    private void convertToBitmap(Bitmap bitmap) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();

        IntBuffer ib = IntBuffer.allocate(w * h);
        GLES20.glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, ib);
        int[] ia = ib.array();

        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(ia));
    }

    /**
     * When the current thread finish renderring and reading pixels, the EGLContext should be unbound.
     * Then the EGLContext could be reused for other threads. Make it possible to share the EGLContext
     * To bind the EGLContext to current Thread, just call eglMakeCurrent()
     */
    private void destroyEglSurface() {
        mEgl.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        if (mEGLSurface != null) {
            mEgl.eglDestroySurface(mEGLDisplay, mEGLSurface);
        }
    }

    private OffScreenBlurRenderer getRenderer() {
        OffScreenBlurRenderer renderer = mThreadRenderer.get();
        if (renderer == null) {
            renderer = new OffScreenBlurRenderer();
            mThreadRenderer.set(renderer);
        }

        return renderer;
    }

    private EGLContext getEGLContext() {
        EGLContext eglContext = mThreadEGLContext.get();
        if (eglContext == null) {
            eglContext = mEgl.eglCreateContext(mEGLDisplay, mEglConfigs[0], EGL10.EGL_NO_CONTEXT, mContextAttrs);
            mThreadEGLContext.set(eglContext);
        }

        return eglContext;
    }

    public void setBlurRadius(int radius) {
        getRenderer().setBlurRadius(radius);
    }

    public void setBlurMode(@Mode int mode) {
        getRenderer().setBlurMode(mode);
    }

    public void free() {
        getRenderer().free();
        EGLContext eglContext = mThreadEGLContext.get();
        if (eglContext != null) {
            mEgl.eglDestroyContext(mEGLDisplay, eglContext);
        }
        mThreadEGLContext.set(null);
    }

}
