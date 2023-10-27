package com.example.hokoblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hokoblurdemo.R;
import com.hoko.blur.HokoBlur;
import com.hoko.blur.task.AsyncBlurTask;

public class EasyBlurActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_blur);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cat);

        final ImageView imageView = findViewById(R.id.image);
        final ImageView imageView1 = findViewById(R.id.image1);
        final ImageView imageView2 = findViewById(R.id.image2);
        final ImageView imageView3 = findViewById(R.id.image3);

        imageView.setImageBitmap(bitmap);
        imageView1.setImageBitmap(HokoBlur.with(this)
                .forceCopy(true)
                .scheme(HokoBlur.SCHEME_RENDER_SCRIPT)
                .sampleFactor(3.0f)
                .radius(20)
                .blur(bitmap));
        HokoBlur.with(this)
                .scheme(HokoBlur.SCHEME_OPENGL)
                .translateX(150)
                .translateY(150)
                .forceCopy(false)
                .sampleFactor(5.0f)
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

        imageView1.post(() -> HokoBlur.with(EasyBlurActivity.this)
                .scheme(HokoBlur.SCHEME_NATIVE)
                .translateX(100)
                .translateY(100)
                .asyncBlur(imageView1, new AsyncBlurTask.Callback() {
                    @Override
                    public void onBlurSuccess(Bitmap bitmap1) {
                        imageView3.setImageBitmap(bitmap1);
                    }

                    @Override
                    public void onBlurFailed(Throwable e) {
                        e.printStackTrace();
                    }
                }));

    }
}
