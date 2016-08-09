package com.example.xiangpi.dynamicblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.blurop.StackBlur;

public class StackBlurActivity extends AppCompatActivity implements View.OnClickListener{


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

        mImageView.setImageResource(R.mipmap.sample);

        mBlurBtn.setOnClickListener(this);
        mNativeBlurBtn.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.blur_btn) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.sample);

                    final int w = bitmap.getWidth();
                    final int h = bitmap.getHeight();
//
//                int[] pixels = new int[w * h];
//                int[] result = new int[w * h];
//                bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

                    final long start = System.currentTimeMillis();

                    final Bitmap blurred = StackBlur.doBlur(bitmap, 10, false);
                    final long stop = System.currentTimeMillis();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(blurred);
                            Toast.makeText(StackBlurActivity.this, (stop - start) + "ms", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
        } else if (id == R.id.native_blur_btn){
            new Thread(new Runnable() {
                @Override
                public void run() {
//
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.sample);

                    final int w = bitmap.getWidth();
                    final int h = bitmap.getHeight();
//
                int[] pixels = new int[w * h];
//                int[] result = new int[w * h];
                bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
//
                    final long start = System.currentTimeMillis();
                    nativeStackBlur(pixels, w, h, 10);
                    final Bitmap blurred = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
                    final long stop = System.currentTimeMillis();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(blurred);
                            Toast.makeText(StackBlurActivity.this, (stop - start) + "ms", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
        }

    }

    public native void nativeStackBlur(int[] pixels, int width, int height, int radius);

    static {
        System.loadLibrary("boxblur");
    }
}
