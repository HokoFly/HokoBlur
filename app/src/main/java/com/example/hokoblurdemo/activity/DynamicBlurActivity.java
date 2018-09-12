package com.example.hokoblurdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.hokoblurdemo.R;
import com.hoko.blur.view.DragBlurringView;

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
