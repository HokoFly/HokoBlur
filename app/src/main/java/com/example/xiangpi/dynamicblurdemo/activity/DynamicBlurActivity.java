package com.example.xiangpi.dynamicblurdemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.xiangpi.dynamicblurdemo.R;
import com.xiangpi.blurlibrary.view.BlurringView;

public class DynamicBlurActivity extends AppCompatActivity {

    private View mBlurredView;

    private BlurringView mBlurringView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_blur);

        mBlurredView = findViewById(R.id.container);

        mBlurringView = (BlurringView) findViewById(R.id.blurring);

        mBlurringView.setBlurredView(mBlurredView);

    }
}
