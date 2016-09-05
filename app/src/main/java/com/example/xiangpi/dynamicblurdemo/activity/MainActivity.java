package com.example.xiangpi.dynamicblurdemo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.xiangpi.dynamicblurdemo.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBoxBlurBtn;
    private Button mGaussianBlurBtn;
    private Button mFastStackBlurBtn;

    private Button mRenderScriptBtn;
    private Button mOpenGLBtn;
    private Button mTexBtn;
    private Button mOffScreenBtn;

    private Button mDynamicBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mBoxBlurBtn = (Button) findViewById(R.id.box_blur);
        mGaussianBlurBtn = (Button) findViewById(R.id.gaussian_blur);
        mFastStackBlurBtn = (Button) findViewById(R.id.fast_stack_blur);
        mRenderScriptBtn = (Button) findViewById(R.id.render_script_demo);
        mOpenGLBtn = (Button) findViewById(R.id.opengl_blur);
        mTexBtn = (Button) findViewById(R.id.tex_blur);
        mDynamicBtn = (Button) findViewById(R.id.dynamic_blur);
        mOffScreenBtn = (Button) findViewById(R.id.offscreen_blur);

        mBoxBlurBtn.setOnClickListener(this);
        mGaussianBlurBtn.setOnClickListener(this);
        mFastStackBlurBtn.setOnClickListener(this);
        mRenderScriptBtn.setOnClickListener(this);
        mOpenGLBtn.setOnClickListener(this);
        mTexBtn.setOnClickListener(this);
        mDynamicBtn.setOnClickListener(this);
        mOffScreenBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        final Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.box_blur:
                intent.setClass(MainActivity.this, BoxBlurActivity.class);
                break;
            case R.id.gaussian_blur:
                break;
            case R.id.fast_stack_blur:
                intent.setClass(MainActivity.this, StackBlurActivity.class);
                break;

            case R.id.render_script_demo:
                intent.setClass(MainActivity.this, RenderScriptActivity.class);
                break;
            case R.id.opengl_blur:
                intent.setClass(MainActivity.this, OpenGLActivity.class);
                break;
            case R.id.tex_blur:
                intent.setClass(MainActivity.this, TextureViewActivity.class);
                break;
            case R.id.dynamic_blur:
                intent.setClass(MainActivity.this, DynamicBlurActivity.class);
                break;
            case R.id.offscreen_blur:
                intent.setClass(MainActivity.this, OffScreenActivity.class);
                break;
        }

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list != null && list.size() > 0) {
            startActivity(intent);
        }

    }

}
