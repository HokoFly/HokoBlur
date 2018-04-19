package com.example.hokoblurdemo.opengl.glsurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.hokoblurdemo.R;
import com.example.hokoblurdemo.opengl.textureview.TextureViewRendererProxy;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by yuxfzju on 16/8/10.
 */
public class BitmapRender implements GLSurfaceView.Renderer{


    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private TextureViewRendererProxy mRectangle;

    private Context mCtx;

    private Bitmap mBitmap;

    public BitmapRender(Context context) {
        mCtx = context;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        mBitmap = BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.sample5, options);
        mRectangle = new TextureViewRendererProxy();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
//
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        mRectangle.draw(mBitmap, mMVPMatrix);
    }
}
