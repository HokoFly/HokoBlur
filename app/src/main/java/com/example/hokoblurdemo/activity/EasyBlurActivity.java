package com.example.hokoblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.hokoblurdemo.R;
import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.EasyBlur;
import com.hoko.blurlibrary.task.AsyncBlurTask;
import com.hoko.blurlibrary.util.BitmapUtil;

public class EasyBlurActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_blur);

         Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.cat);

        final ImageView imageView = ((ImageView) findViewById(R.id.image));
        final ImageView imageView1 = ((ImageView) findViewById(R.id.image1));
        final ImageView imageView2 = ((ImageView) findViewById(R.id.image2));

        imageView.setImageBitmap(bitmap);
        EasyBlur.blur(bitmap, 20);
//        imageView.setImageBitmap(EasyBlur.makeBlur(bitmap, bitmap.getWidth(), bitmap.height(), 0, 0, 2, 10));
//        Blur.with(this).forceCopy(false).scheme(Blur.SCHEME_RENDER_SCRIPT).sampleFactor(1.0f).radius(20).blurGenerator().asyncBlur(bitmap, new AsyncBlurTask.CallBack() {
//            @Override
//            public void onBlurSuccess(Bitmap bitmap) {
//                imageView.setImageBitmap(bitmap);
//            }
//
//            @Override
//            public void onBlurFailed() {
//            }
//        });
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.cat);
        Blur.with(this).forceCopy(true).scheme(Blur.SCHEME_NATIVE).sampleFactor(2.0f).radius(2).blurGenerator().asyncBlur(bitmap, new AsyncBlurTask.CallBack() {
            @Override
            public void onBlurSuccess(Bitmap bitmap) {
                imageView1.setImageBitmap(bitmap);
            }

            @Override
            public void onBlurFailed() {
            }
        });
        Blur.with(this).scheme(Blur.SCHEME_OPENGL).blurGenerator().asyncBlur(bitmap, new AsyncBlurTask.CallBack() {
            @Override
            public void onBlurSuccess(Bitmap bitmap) {
                imageView2.setImageBitmap(bitmap);
            }

            @Override
            public void onBlurFailed() {
            }
        });
    }
}
