package com.example.xiangpi.dynamicblurdemo.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by xiangpi on 16/8/17.
 */
public class GLRendererImpl implements GLProducerThread.GLRenderer{

    private Context mCtx;
    private Rectangle mRectangle;

    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mWidth;
    private int mHeight;


    public GLRendererImpl(Context context) {
        mCtx = context;
    }

    public void initGLRenderer() {
        mRectangle = new Rectangle(mCtx);

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);

    }

    public void setViewport(int width, int height) {
        mWidth = width;
        mHeight = height;

        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glViewport(0, 0, mWidth, mHeight);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        mRectangle.draw(mMVPMatrix);

    }
}
