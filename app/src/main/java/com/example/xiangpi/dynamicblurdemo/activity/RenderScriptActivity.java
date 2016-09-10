package com.example.xiangpi.dynamicblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.util.ImageUtils;
import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.generator.IBlur;

public class RenderScriptActivity extends AppCompatActivity implements View.OnClickListener{

    private RenderScript mRenderScript;

    private Bitmap mBitmapIn;
    private Bitmap mBitmapOut;

    private Button mBoxBlurBtn;
    private Button mStackBlurBtn;
    private Button mGaussianBlurBtn;

    private ImageView mImageView;
    private IBlur mBoxBlurGenerator;
    private IBlur mGaussianBlurGenerator;
    private IBlur mStackBlurGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_script);

        mBoxBlurBtn = (Button) findViewById(R.id.box_blur_btn);
        mStackBlurBtn = (Button) findViewById(R.id.stack_blur_btn);
        mGaussianBlurBtn = (Button) findViewById(R.id.gaussian_blur_btn);
        mImageView = (ImageView) findViewById(R.id.photo);

        mRenderScript = RenderScript.create(this);
        mBitmapIn = BitmapFactory.decodeResource(getResources(), ImageUtils.testImageRes);
        mBitmapOut = Bitmap.createBitmap(mBitmapIn.getWidth(), mBitmapIn.getHeight(), Bitmap.Config.ARGB_8888);

        mImageView.setImageBitmap(mBitmapIn);

        mBoxBlurBtn.setOnClickListener(this);
        mStackBlurBtn.setOnClickListener(this);
        mGaussianBlurBtn.setOnClickListener(this);

        mBoxBlurGenerator = Blur.with(this).mode(Blur.BlurMode.BOX).scheme(Blur.BlurScheme.RENDER_SCRIPT).getBlurGenerator();
        mStackBlurGenerator = Blur.with(this).mode(Blur.BlurMode.STACK).scheme(Blur.BlurScheme.RENDER_SCRIPT).getBlurGenerator();
        mGaussianBlurGenerator = Blur.with(this).mode(Blur.BlurMode.GAUSSIAN).scheme(Blur.BlurScheme.RENDER_SCRIPT).getBlurGenerator();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRenderScript.destroy();
    }

    @Override
    public void onClick(final View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (view.getId()) {
                    case R.id.box_blur_btn:
                        mBitmapOut = mBoxBlurGenerator.doBlur(mBitmapIn);
                        break;
                    case R.id.stack_blur_btn:
                        mBitmapOut = mStackBlurGenerator.doBlur(mBitmapIn);
                        break;
                    case R.id.gaussian_blur_btn:
                        mBitmapOut = mGaussianBlurGenerator.doBlur(mBitmapIn);
                        break;
                    default:
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(mBitmapOut);
                    }
                });
            }
        }).start();

    }
}
