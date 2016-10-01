package com.hoko.blurlibrary.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.hoko.blurlibrary.Blur;


/**
 * Created by xiangpi on 16/8/29.
 */
public class OffScreenRendererImpl implements GLRenderer {

    private OffScreenRectangle mRectangle;

    private Bitmap mBitmap;

    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public OffScreenRendererImpl(Bitmap bitmap, int radius, @Blur.BlurMode int mode) {
        mBitmap = bitmap;
        mRectangle = new OffScreenRectangle(radius, mode);
    }

    @Override
    public void onDrawFrame() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
//        mRectangle = new OffScreenRectangle(mBitmap);
        mRectangle.setInputBitmap(mBitmap);

        mRectangle.draw(mMVPMatrix);


    }

    @Override
    public void onSurfaceCreated() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
//        float ratio = (float) width / height;
        float ratio = 1.0f;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public Bitmap getInputBitmap() {
        return mBitmap;
    }
}
