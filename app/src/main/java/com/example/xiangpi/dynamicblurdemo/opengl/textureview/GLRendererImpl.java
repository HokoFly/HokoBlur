package com.example.xiangpi.dynamicblurdemo.opengl.textureview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.opengl.Rectangle;
import com.xiangpi.blurlibrary.opengl.offscreen.GLRenderer;

/**
 * Created by xiangpi on 16/8/17.
 */
public class GLRendererImpl implements GLRenderer {

    private Context mCtx;
    private Rectangle mRectangle;

    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mWidth;
    private int mHeight;

    private Bitmap mBitmap;

    public GLRendererImpl(Context context) {
        mCtx = context;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        mBitmap = BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.test_wallpaper, options);
    }

    public void initGLRenderer() {
        mRectangle = new Rectangle(mBitmap);

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

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public Bitmap getInputBitmap() {
        return mBitmap;
    }
}
