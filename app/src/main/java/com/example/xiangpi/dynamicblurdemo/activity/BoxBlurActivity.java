package com.example.xiangpi.dynamicblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.xiangpi.dynamicblurdemo.blurop.BoxBlur;
import com.example.xiangpi.dynamicblurdemo.R;

public class BoxBlurActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mImageView;
    private Button mBlurBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_blur);

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
                int[] result = new int[w * h];
                bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

                final long start = System.currentTimeMillis();

//                BoxBlur.blur(pixels, result, w, h, 10);
//                BoxBlur.blur(result, pixels, h, w, 10);
                BoxBlur.fastBlur(pixels, w, h, 10);
                final Bitmap blurred = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
//                final Bitmap blurred = BoxBlur.blur(20, bitmap);
                final long stop = System.currentTimeMillis();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(blurred);
                        Toast.makeText(BoxBlurActivity.this, (stop - start) + "ms", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}
