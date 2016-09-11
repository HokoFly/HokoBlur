package com.example.xiangpi.dynamicblurdemo.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.xiangpi.dynamicblurdemo.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mMultiBlurBtn;
    private Button mOpenGLBtn;
    private Button mTexBtn;
    private Button mDynamicBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mMultiBlurBtn = (Button) findViewById(R.id.multi_blur);
        mOpenGLBtn = (Button) findViewById(R.id.opengl_blur);
        mTexBtn = (Button) findViewById(R.id.tex_blur);
        mDynamicBtn = (Button) findViewById(R.id.dynamic_blur);

        mMultiBlurBtn.setOnClickListener(this);
        mOpenGLBtn.setOnClickListener(this);
        mTexBtn.setOnClickListener(this);
        mDynamicBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        final Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.multi_blur:
                intent.setClass(MainActivity.this, MultiBlurActivity.class);
                break;
            case R.id.opengl_blur:
                intent.setClass(MainActivity.this, GLSurfaceActivity.class);
                break;
            case R.id.tex_blur:
                intent.setClass(MainActivity.this, TextureViewActivity.class);
                break;
            case R.id.dynamic_blur:
                intent.setClass(MainActivity.this, DynamicBlurActivity.class);
                break;
        }

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list != null && list.size() > 0) {
            startActivity(intent);
        }

    }

}
