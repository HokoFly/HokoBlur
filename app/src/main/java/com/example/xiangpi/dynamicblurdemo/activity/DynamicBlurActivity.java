package com.example.xiangpi.dynamicblurdemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.xiangpi.dynamicblurdemo.R;
import com.hoko.blurlibrary.view.DragBlurringView;

public class DynamicBlurActivity extends AppCompatActivity {

    private View mBlurredView;

    private DragBlurringView mDragBlurringView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_blur);

        mBlurredView = findViewById(R.id.container);

        mDragBlurringView = (DragBlurringView) findViewById(R.id.blurring);

        mDragBlurringView.setBlurredView(mBlurredView);

    }
}
