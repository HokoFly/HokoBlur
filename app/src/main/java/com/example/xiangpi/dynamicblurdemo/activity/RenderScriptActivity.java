package com.example.xiangpi.dynamicblurdemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v8.renderscript.*;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.xiangpi.dynamicblurdemo.R;

import java.util.Arrays;

public class RenderScriptActivity extends AppCompatActivity implements View.OnClickListener{

    private static final float BLUR_KERNEL_RADIUS = 10f;

    private RenderScript mRenderScript;

    private Bitmap mBitmapIn;
    private Bitmap mBitmapOut;

    private Allocation mAllocationIn;
    private Allocation mAllocationOut;

    private ScriptC_invert mScriptInvert;
    private ScriptIntrinsicBlur mScriptBlur;
    private ScriptC_boxblur mScriptBoxBlur;

    private Button mInvertBtn;
    private Button mBoxBlurBtn;
    private Button mStackBlurBtn;
    private Button mGaussianBlurBtn;

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_script);

        mInvertBtn = (Button) findViewById(R.id.invert_btn);
        mBoxBlurBtn = (Button) findViewById(R.id.box_blur_btn);
        mStackBlurBtn = (Button) findViewById(R.id.stack_blur_btn);
        mGaussianBlurBtn = (Button) findViewById(R.id.gaussian_blur_btn);
        mImageView = (ImageView) findViewById(R.id.photo);

        mRenderScript = RenderScript.create(this);
        mBitmapIn = BitmapFactory.decodeResource(getResources(), R.mipmap.sample);
        mBitmapOut = Bitmap.createBitmap(mBitmapIn.getWidth(), mBitmapIn.getHeight(), Bitmap.Config.ARGB_8888);

        mImageView.setImageBitmap(mBitmapIn);
        mAllocationIn = Allocation.createFromBitmap(mRenderScript, mBitmapIn);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, mBitmapIn);

        mScriptInvert = new ScriptC_invert(mRenderScript);

        mScriptBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        mScriptBlur.setRadius(BLUR_KERNEL_RADIUS);

        mScriptBoxBlur = new ScriptC_boxblur(mRenderScript);

        mInvertBtn.setOnClickListener(this);
        mBoxBlurBtn.setOnClickListener(this);
        mGaussianBlurBtn.setOnClickListener(this);

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
                final long start = System.currentTimeMillis();

                switch (view.getId()) {
                    case R.id.invert_btn:
                        mScriptInvert.forEach_invert(mAllocationIn, mAllocationOut);
                        break;

                    case R.id.box_blur_btn:
                        mScriptBoxBlur.set_input(mAllocationIn);
                        mScriptBoxBlur.set_output(mAllocationOut);
                        mScriptBoxBlur.set_radius((int) BLUR_KERNEL_RADIUS);
                        mScriptBoxBlur.forEach_boxblur(mAllocationIn);

                        break;
                    case R.id.stack_blur_btn:
                        break;
                    case R.id.gaussian_blur_btn:
                        mScriptBlur.setInput(mAllocationIn);
                        mScriptBlur.forEach(mAllocationOut);
                        break;
                }

                final long stop = System.currentTimeMillis();
                mAllocationOut.copyTo(mBitmapOut);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(mBitmapOut);
                        Toast.makeText(RenderScriptActivity.this, (stop - start) + "ms", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();

    }
}
