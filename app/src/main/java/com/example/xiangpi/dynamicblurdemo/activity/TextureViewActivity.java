package com.example.xiangpi.dynamicblurdemo.activity;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;

import com.example.xiangpi.dynamicblurdemo.opengl.textureview.GLProducerThread;
import com.example.xiangpi.dynamicblurdemo.opengl.textureview.GLRendererImpl;

public class TextureViewActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private TextureView mTextureView;
    private GLRendererImpl mGLRenderer;
    private GLProducerThread mGLThread;

    private boolean mRunDraw = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(this);

        setContentView(mTextureView);

        mGLRenderer = new GLRendererImpl(this);
        if(mTextureView.isAvailable()) {
            onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mRunDraw = false;
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int w, int h) {
        mGLRenderer.setViewport(w, h);
        mGLThread = new GLProducerThread(mGLRenderer, surfaceTexture, mRunDraw);
        mGLThread.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int w, int h) {
        mGLRenderer.setViewport(w, h);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mRunDraw = false;
        mGLThread = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
