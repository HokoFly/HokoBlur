package com.hoko.blurlibrary.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.anno.Mode;
import com.hoko.blurlibrary.util.ShaderUtil;

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
public class EglBuffer {

    private EGL10 mEgl;

    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private static final int EGL_OPENGL_ES2_BIT = 4;

    private EGLConfig[] mEglConfigs = new EGLConfig[1];
    private int[] mContextAttribs;

    //EGLContext、EGLSurface和Renderer都只与当前线程关联，进行渲染，因此采用ThreadLocal线程隔离。
    private ThreadLocal<OffScreenRendererImpl> mThreadRenderer = new ThreadLocal<OffScreenRendererImpl>();

    private ThreadLocal<EGLContext> mThreadEGLContext = new ThreadLocal<EGLContext>();

    private ThreadLocal<EGLSurface> mThreadEGLSurface = new ThreadLocal<EGLSurface>();

    public EglBuffer() {
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

    }

    private void initSurface(int width, int height) {
        int[] surfaceAttribs = {
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL10.EGL_NONE
        };

        EGLSurface eglSurface = mEgl.eglCreatePbufferSurface(mEGLDisplay, mEglConfigs[0], surfaceAttribs);

        mThreadEGLSurface.set(eglSurface);
        mEgl.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, getEGLContext());

    }


    public Bitmap getBlurBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return bitmap;
        }

        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();

        initSurface(w, h);

        if (getRenderer() != null) {
            getRenderer().onSurfaceCreated();
            getRenderer().onSurfaceChanged(w, h);
            getRenderer().onDrawFrame(bitmap);
            mEgl.eglSwapBuffers(mEGLDisplay, mThreadEGLSurface.get());
        }
        convertToBitmap(bitmap);
        unbindEglCurrent();
        return bitmap;

    }

    public void setBlurRadius(int radius) {
        getRenderer().setBlurRadius(radius);
    }

    public void setBlurMode(@Mode int mode) {
        getRenderer().setBlurMode(mode);
    }

    public void free() {
        getRenderer().free();
    }

    private void convertToBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled() || !bitmap.isMutable()) {
            return;
        }

        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();

        IntBuffer ib = IntBuffer.allocate(w * h);
        GLES20.glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, ib);
        int[] ia = ib.array();

//        for (int i = 0; i < mHeight; i++) {
//            for (int j = 0; j < mWidth; j++) {
//                iat[(mHeight - i - 1) * mWidth + j] = ia[i * mWidth + j];
//            }
//        }

        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(ia));
//        mOutputBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//        mOutputBitmap.copyPixelsFromBuffer(IntBuffer.wrap(ia));
    }

    /**
     * 在当前线程结束一系列渲染和像素读取操作之后，需要将EGLContext与当前线程解绑，
     * 这样才能在下次模糊操作的另一个线程中，继续使用当前EGLContext，达到共享EGLContext的目的。
     * 当前线程绑定EGLContext，只需调用eglMakeCurrent()
     */
    private void unbindEglCurrent() {
        mEgl.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);

    }

    private OffScreenRendererImpl getRenderer() {
        OffScreenRendererImpl renderer = mThreadRenderer.get();
        if (renderer == null) {
            renderer = new OffScreenRendererImpl();
            mThreadRenderer.set(renderer);
        }

        return renderer;
    }

    private EGLContext getEGLContext() {
        EGLContext eglContext = mThreadEGLContext.get();
        if (eglContext == null) {
            eglContext = mEgl.eglCreateContext(mEGLDisplay, mEglConfigs[0], EGL10.EGL_NO_CONTEXT, mContextAttribs);
            mThreadEGLContext.set(eglContext);
        }

        return eglContext;
    }
}
