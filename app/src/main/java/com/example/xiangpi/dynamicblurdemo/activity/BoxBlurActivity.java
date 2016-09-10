package com.example.xiangpi.dynamicblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.generator.IBlur;
import com.xiangpi.blurlibrary.origin.BoxBlur;
import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.util.ImageUtils;

public class BoxBlurActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImageView;
    private Button mBlurBtn;
    private Button mNativeBlurBtn;
    private Bitmap mInBitmap;
    private Bitmap mOutBitmap;
    private IBlur mOriginBlurGenerator;
    private IBlur mNativeBlurGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_blur);

        mImageView = (ImageView) findViewById(R.id.photo);
        mBlurBtn = (Button) findViewById(R.id.blur_btn);
        mNativeBlurBtn = (Button) findViewById(R.id.native_blur_btn);

        mImageView.setImageResource(ImageUtils.testImageRes);

        mBlurBtn.setOnClickListener(this);
        mNativeBlurBtn.setOnClickListener(this);
        mInBitmap = BitmapFactory.decodeResource(getResources(), ImageUtils.testImageRes);
        mOriginBlurGenerator = Blur.with(this).mode(Blur.BlurMode.BOX).scheme(Blur.BlurScheme.JAVA).getBlurGenerator();
        mNativeBlurGenerator = Blur.with(this).mode(Blur.BlurMode.BOX).scheme(Blur.BlurScheme.NATIVE).getBlurGenerator();
    }


    @Override
    public void onClick(View v) {
        final int id = v.getId();

        if (id == R.id.blur_btn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < ImageUtils.blurCount; i++) {
                        mOutBitmap = mOriginBlurGenerator.doBlur(mInBitmap);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(mOutBitmap);
                        }
                    });
                }
            }).start();
        } else if (id == R.id.native_blur_btn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < ImageUtils.blurCount; i++) {
                        mOutBitmap = mNativeBlurGenerator.doBlur(mInBitmap);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(mOutBitmap);
                        }
                    });
                }
            }).start();
        }

    }

}
