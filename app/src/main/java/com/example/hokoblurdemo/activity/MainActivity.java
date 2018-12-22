package com.example.hokoblurdemo.activity;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.example.hokoblurdemo.R;
import com.hoko.blur.opengl.drawable.BlurDrawable;
import com.hoko.blur.view.BlurLinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean mHasBlurred;
    private BlurDrawable mBlurDrawable;

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
        Button layoutBtn = findViewById(R.id.layout_blur);
        BlurLinearLayout blurLayout = findViewById(R.id.blur_layout);
        Button drawableBtn = findViewById(R.id.drawable_blur);
        Button easyBlurBtn = findViewById(R.id.easy_blur);

        multiBlurBtn.setOnClickListener(this);
        dynamicBtn.setOnClickListener(this);
        layoutBtn.setOnClickListener(this);
        drawableBtn.setOnClickListener(this);
        easyBlurBtn.setOnClickListener(this);

        mBlurDrawable = blurLayout.getBlurDrawable();

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
            case R.id.layout_blur:
                blurBackground();
                break;
            case R.id.drawable_blur:
                intent.setClass(MainActivity.this, BlurDrawableActivity.class);
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


    @Override
    public void onAttachedToWindow() {


        super.onAttachedToWindow();
        if (mHasBlurred) {
            return;
        }

        mHasBlurred = true;

        blurBackground();
    }

    private void blurBackground() {
        ValueAnimator animator = ValueAnimator.ofInt(0, 10);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int r = (int) animation.getAnimatedValue();
                mBlurDrawable.radius(r);
            }
        });
        animator.setDuration(1000);
        animator.start();
    }
}
