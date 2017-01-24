package com.example.xiangpi.dynamicblurdemo.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.example.xiangpi.dynamicblurdemo.R;

public class BlurDrawableActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur_drawable);


    }

    public void remove(View view) {
        ((ViewGroup) view.getParent()).removeView(view);
    }
}
