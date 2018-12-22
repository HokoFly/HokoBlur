package com.example.hokoblurdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.hokoblurdemo.R;
import com.hoko.blur.view.DragBlurringView;

public class DynamicBlurActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_blur);

        View blurredView = findViewById(R.id.container);

        DragBlurringView dragBlurringView = findViewById(R.id.blurring);

        dragBlurringView.setBlurredView(blurredView);

    }
}
