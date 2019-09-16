package com.example.hokoblurdemo.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hokoblurdemo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button multiBlurBtn = findViewById(R.id.multi_blur);
        Button dynamicBtn = findViewById(R.id.dynamic_blur);
        Button easyBlurBtn = findViewById(R.id.easy_blur);

        multiBlurBtn.setOnClickListener(this);
        dynamicBtn.setOnClickListener(this);
        easyBlurBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        final Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.multi_blur:
                intent.setClass(MainActivity.this, MultiBlurActivity.class);
                break;
            case R.id.dynamic_blur:
                intent.setClass(MainActivity.this, DynamicBlurActivity.class);
                break;
            case R.id.easy_blur:
                intent.setClass(MainActivity.this, EasyBlurActivity.class);
                break;

        }

        ComponentName componentName = intent.resolveActivity(getPackageManager());
        if (componentName != null) {
            startActivity(intent);
        }

    }

}
