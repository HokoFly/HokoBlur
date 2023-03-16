package com.hoko.blur.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.hoko.blur.anno.Mode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    private static final EGL10 EGL = (EGL10) EGLContext.getEGL();
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int[] CONFIG_ATTRIB_LIST = {
            EGL10.EGL_BUFFER_SIZE, 32,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
            EGL10.EGL_NONE
    };
    private final int[] CONTEXT_ATTRIB_LIST = new int[]{
            EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
    };

    public Bitmap getBlurBitmap(Bitmap bitmap, int radius, @Mode int mode) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();
        EGLDisplay eglDisplay = EGL10.EGL_NO_DISPLAY;
        EGLSurface eglSurface = null;
        EGLContext eglContext = null;
        EGLConfig[] eglConfigs = new EGLConfig[1];
        OffScreenBlurRenderer renderer = null;
        try {
            eglDisplay = createDisplay(eglConfigs);
            eglSurface = createSurface(w, h, eglDisplay, eglConfigs);
            eglContext = createEGLContext(eglDisplay, eglConfigs);
            EGL.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
            renderer = createRenderer(radius, mode);
            renderer.onDrawFrame(bitmap);
            EGL.eglSwapBuffers(eglDisplay, eglSurface);
            convertToBitmap(bitmap);
        } catch (Throwable t) {
            Log.e(TAG, "Blur the bitmap error", t);
        } finally {
            destroyEglSurface(eglDisplay, eglSurface);
            destroyEglContext(eglDisplay, eglContext);
        }
        return bitmap;
    }


    private void convertToBitmap(Bitmap bitmap) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();
        ByteBuffer buffer = null;
        try {
            buffer = ByteBuffer.allocateDirect(w * h * 4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            GLES20.glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            buffer.rewind();
            bitmap.copyPixelsFromBuffer(buffer);
        } finally {
            if (buffer != null) {
                buffer.clear();
                buffer = null;
            }
        }
    }

    private EGLDisplay createDisplay(EGLConfig[] eglConfigs) {
        EGLDisplay eglDisplay = EGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        EGL.eglInitialize(eglDisplay, new int[2]);
        EGL.eglChooseConfig(eglDisplay, CONFIG_ATTRIB_LIST, eglConfigs, 1, new int[1]);
        return eglDisplay;
    }

    private EGLSurface createSurface(int width, int height, EGLDisplay eglDisplay, EGLConfig[] eglConfigs) {
        int[] surfaceAttrs = {
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL10.EGL_NONE
        };
        return EGL.eglCreatePbufferSurface(eglDisplay, eglConfigs[0], surfaceAttrs);
    }

    private void destroyEglSurface(EGLDisplay eglDisplay, EGLSurface eglSurface) {
        EGL.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        if (eglSurface != null) {
            EGL.eglDestroySurface(eglDisplay, eglSurface);
        }
    }

    private EGLContext createEGLContext(EGLDisplay eglDisplay, EGLConfig[] eglConfigs) {
        return EGL.eglCreateContext(eglDisplay, eglConfigs[0], EGL10.EGL_NO_CONTEXT, CONTEXT_ATTRIB_LIST);
    }

    private void destroyEglContext(EGLDisplay eglDisplay, EGLContext eglContext) {
        EGL.eglDestroyContext(eglDisplay, eglContext);
    }

    private OffScreenBlurRenderer createRenderer(int radius, @Mode int mode) {
        OffScreenBlurRenderer renderer = new OffScreenBlurRenderer();
        renderer.setBlurRadius(radius);
        renderer.setBlurMode(mode);
        return renderer;
    }

}
