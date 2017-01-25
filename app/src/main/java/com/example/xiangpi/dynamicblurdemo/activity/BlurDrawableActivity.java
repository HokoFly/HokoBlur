package com.example.xiangpi.dynamicblurdemo.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.example.xiangpi.dynamicblurdemo.R;
import com.hoko.blurlibrary.opengl.drawable.BlurDrawable;

public class BlurDrawableActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur_drawable);

        findViewById(R.id.test_view).setBackgroundDrawable(new BlurDrawable());


    }

    public void remove(View view) {
        ((ViewGroup) view.getParent()).removeView(view);
    }
}
