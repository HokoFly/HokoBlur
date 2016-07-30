package com.example.xiangpi.dynamicblurdemo.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.xiangpi.dynamicblurdemo.blurop.BoxBlur;
import com.example.xiangpi.dynamicblurdemo.R;

public class BoxBlurActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImageView;
    private Button mBlurBtn;
    private Button mNativeBlurBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_blur);

        mImageView = (ImageView) findViewById(R.id.photo);
        mBlurBtn = (Button) findViewById(R.id.blur_btn);
        mNativeBlurBtn = (Button) findViewById(R.id.native_blur_btn);

        mImageView.setImageResource(R.mipmap.sample1);

        mBlurBtn.setOnClickListener(this);
        mNativeBlurBtn.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        final int id = v.getId();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.sample);

        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();

        final int[] pixels = new int[w * h];
        int[] result = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        if (id == R.id.blur_btn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
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
        } else if (id == R.id.native_blur_btn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final long start = System.currentTimeMillis();

                    nativeBoxBlur(pixels, w, h, 10);
                    final Bitmap blurred = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
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

    static {
        System.loadLibrary("boxblur");
    }

    public native void nativeBoxBlur(int[] pixels, int width, int height, int radius);

}
