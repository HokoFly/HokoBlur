package com.example.xiangpi.dynamicblurdemo.opengl.textureview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.opengl.shape.Rectangle;
import com.hoko.blurlibrary.opengl.offscreen.GLRenderer;

/**
 * Created by xiangpi on 16/8/17.
 */
public class TextureViewRendererImpl implements GLRenderer {

    private Rectangle mRectangle;

    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mWidth;
    private int mHeight;

    public TextureViewRendererImpl() {
        mRectangle = new Rectangle();
    }


    @Override
    public void onDrawFrame(Bitmap bitmap) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        mRectangle.draw(bitmap, mMVPMatrix);

    }

    @Override
    public void onSurfaceCreated() {
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
        GLES20.glViewport(0, 0, mWidth, mHeight);

        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

}
