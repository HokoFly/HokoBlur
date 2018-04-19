package com.example.hokoblurdemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hokoblurdemo.R;
import com.example.hokoblurdemo.opengl.glsurfaceview.BlurGLSurfaceView;


public class GLSurfaceActivity extends AppCompatActivity {

    private BlurGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_open_gl);
        mGLSurfaceView = (BlurGLSurfaceView) findViewById(R.id.blur_glsurfaceview);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

}
