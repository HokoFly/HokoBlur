package com.example.xiangpi.dynamicblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.xiangpi.dynamicblurdemo.R;
import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.BlurEffectMaker;
import com.hoko.blurlibrary.task.BlurTask;

public class BlurEffectMakerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur_effect_maker);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.cat, options);

        final ImageView imageView = ((ImageView) findViewById(R.id.image));
        final ImageView imageView1 = ((ImageView) findViewById(R.id.image1));
        final ImageView imageView2 = ((ImageView) findViewById(R.id.image2));
//        imageView.setImageBitmap(BlurEffectMaker.makeBlur(bitmap, bitmap.getWidth(), bitmap.getHeight(), 0, 0, 1, 20));
//        imageView.setImageBitmap(Blur.with(this).forceCopy(false).scheme(Blur.SCHEME_NATIVE).sampleFactor(1.0f).blurGenerator().doBlur(bitmap));
        Blur.with(this).forceCopy(false).scheme(Blur.SCHEME_RENDER_SCRIPT).sampleFactor(1.0f).radius(20).blurGenerator().doAsyncBlur(bitmap, new BlurTask.CallBack() {
            @Override
            public void onBlurSuccess(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBlurFailed() {
            }
        });
        Blur.with(this).forceCopy(false).scheme(Blur.SCHEME_RENDER_SCRIPT).sampleFactor(1.0f).radius(20).blurGenerator().doAsyncBlur(bitmap, new BlurTask.CallBack() {
            @Override
            public void onBlurSuccess(Bitmap bitmap) {
                imageView1.setImageBitmap(bitmap);
            }

            @Override
            public void onBlurFailed() {
            }
        });
        Blur.with(this).blurGenerator().doAsyncBlur(bitmap, new BlurTask.CallBack() {
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
