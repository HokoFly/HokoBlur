package com.example.xiangpi.dynamicblurdemo.opengl.offline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.opengl.GLRenderer;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

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

    private GL mGL;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private static final int EGL_OPENGL_ES2_BIT = 4;

    private int mWidth;
    private int mHeight;

    private GLRenderer mRenderer;
    private Context mCtx;

    private Bitmap mOutputBitmap;

    public OffScreenBuffer(Context context) {
        mCtx = context;
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

        EGLConfig[]configs = new EGLConfig[1];

        mEgl.eglChooseConfig(mEGLDisplay, configAttribs, configs, 1, numConfigs);

        int []contextAttribs = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };

        int[] surfaceAttribs = {
                EGL10.EGL_WIDTH, mWidth,
                EGL10.EGL_HEIGHT, mHeight,
                EGL10.EGL_NONE
        };

        mEGLContext = mEgl.eglCreateContext(mEGLDisplay, configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs);
        mEGLSurface = mEgl.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs);

        mEgl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);

        mGL = mEGLContext.getGL();
    }

    public void setRenderer(GLRenderer renderer) {
        mRenderer = renderer;

        mWidth = mRenderer.getInputBitmap().getWidth();
        mHeight = mRenderer.getInputBitmap().getHeight();
        initGL();

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

        return mOutputBitmap;

    }

    private void convertToBitmap() {
        int[] iat = new int[mWidth * mHeight];
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
//        mGL.glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, ib);
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, ib);
        int[] ia = ib.array();

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                iat[(mHeight - i - 1) * mWidth + j] = ia[i * mWidth + j];
            }
        }

        mOutputBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mOutputBitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat));
    }
}
