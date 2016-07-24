package com.example.xiangpi.dynamicblurdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mImageView;
    private Button mBlurBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.photo);
        mBlurBtn = (Button) findViewById(R.id.blur_btn);

        mImageView.setImageResource(R.mipmap.sample1);

        mBlurBtn.setOnClickListener(this);

    }



    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.sample1);

                final int w = bitmap.getWidth();
                final int h = bitmap.getHeight();

                int[] pixels = new int[w * h];
                bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

                final long start = System.currentTimeMillis();

                final Bitmap blurred = BoxBlur.blur(10, bitmap);
                final long stop = System.currentTimeMillis();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(blurred);
                        Toast.makeText(MainActivity.this, (stop - start) + "ms", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}
