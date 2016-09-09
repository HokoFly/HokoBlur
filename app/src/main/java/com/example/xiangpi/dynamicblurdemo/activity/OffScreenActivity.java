package com.example.xiangpi.dynamicblurdemo.activity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.util.ImageUtils;
import com.example.xiangpi.dynamicblurdemo.util.PermissionUtil;
import com.xiangpi.blurlibrary.generator.OpenGLBlurGenerator;

public class OffScreenActivity extends AppCompatActivity {

    private Button mButton;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_screen);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.sample, options);

        mButton = (Button) findViewById(R.id.get_bitmap);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImageUtils.saveBlurredImage(OpenGLBlurGenerator.getInstance().doBlur(mBitmap));
            }
        });

        PermissionUtil.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this);
    }
}
