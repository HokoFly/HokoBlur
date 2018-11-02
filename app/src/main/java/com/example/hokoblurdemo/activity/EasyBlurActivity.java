package com.example.hokoblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.hokoblurdemo.R;
import com.hoko.blur.HokoBlur;
import com.hoko.blur.task.AsyncBlurTask;

public class EasyBlurActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_blur);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.cat);

        final ImageView imageView = ((ImageView) findViewById(R.id.image));
        final ImageView imageView1 = ((ImageView) findViewById(R.id.image1));
        final ImageView imageView2 = ((ImageView) findViewById(R.id.image2));
        final ImageView imageView3 = ((ImageView) findViewById(R.id.image3));

        imageView.setImageBitmap(HokoBlur.with(this)
                .forceCopy(true)
                .scheme(HokoBlur.SCHEME_RENDER_SCRIPT)
                .sampleFactor(3.0f)
                .radius(20)
                .processor()
                .blur(bitmap));

        imageView1.setImageBitmap(bitmap);
//        HokoBlur.with(this).forceCopy(true).scheme(HokoBlur.SCHEME_NATIVE).sampleFactor(2.0f).radius(2).processor().asyncBlur(bitmap, new AsyncBlurTask.Callback() {
//            @Override
//            public void onBlurSuccess(Bitmap bitmap) {
//                imageView1.setImageBitmap(bitmap);
//            }
//
//            @Override
//            public void onBlurFailed() {
//            }
//        });
        HokoBlur.with(this)
                .scheme(HokoBlur.SCHEME_OPENGL)
                .translateX(150)
                .translateY(150)
                .forceCopy(false)
                .sampleFactor(5.0f)
                .needUpscale(true)
                .processor()
                .asyncBlur(bitmap, new AsyncBlurTask.Callback() {
                    @Override
                    public void onBlurSuccess(Bitmap bitmap) {
                        imageView2.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBlurFailed(Throwable e) {
                        e.printStackTrace();
                    }
                });

        imageView1.post(new Runnable() {
            @Override
            public void run() {
                HokoBlur.with(EasyBlurActivity.this)
                        .scheme(HokoBlur.SCHEME_NATIVE)
                        .translateX(100)
                        .translateY(100)
                        .processor()
                        .asyncBlur(imageView1, new AsyncBlurTask.Callback() {
                            @Override
                            public void onBlurSuccess(Bitmap bitmap) {
                                imageView3.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onBlurFailed(Throwable e) {
                                e.printStackTrace();
                            }
                        });

            }
        });

        imageView1.post(new Runnable() {
            @Override
            public void run() {
                HokoBlur.with(EasyBlurActivity.this)
                        .scheme(HokoBlur.SCHEME_RENDER_SCRIPT)
                        .processor()
                        .newBuilder()
                        .processor()
                        .asyncBlur(imageView1, new AsyncBlurTask.Callback() {
                            @Override
                            public void onBlurSuccess(Bitmap bitmap) {
                                imageView3.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onBlurFailed(Throwable e) {
                                e.printStackTrace();
                            }
                        });

            }
        });

    }
}
