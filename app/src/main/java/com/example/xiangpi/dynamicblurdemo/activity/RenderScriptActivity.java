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

public class RenderScriptActivity extends AppCompatActivity {

    private RenderScript mRenderScript;

    private Allocation mAllocationIn;
    private Allocation mAllocationOut;

    private ScriptC_invert mScript;
    private ScriptIntrinsicBlur mScriptBlur;

    private Button mInvertBtn;
    private Button mBlurBtn;

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_script);

        mInvertBtn = (Button) findViewById(R.id.invert_btn);
        mBlurBtn = (Button) findViewById(R.id.blur_btn);
        mImageView = (ImageView) findViewById(R.id.photo);

        mRenderScript = RenderScript.create(this);
        final Bitmap bitmapIn = BitmapFactory.decodeResource(getResources(), R.mipmap.sample1);
        final Bitmap bitmapOut = Bitmap.createBitmap(bitmapIn.getWidth(), bitmapIn.getHeight(), Bitmap.Config.ARGB_8888);

        mImageView.setImageBitmap(bitmapIn);
        mAllocationIn = Allocation.createFromBitmap(mRenderScript, bitmapIn);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, bitmapOut);

        mScript = new ScriptC_invert(mRenderScript);
        mInvertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mScript.forEach_invert(mAllocationIn, mAllocationOut);
                        mAllocationOut.copyTo(bitmapOut);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(bitmapOut);
                            }
                        });
                    }
                }).start();

            }
        });

        mScriptBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        mScriptBlur.setRadius(10f);

        mBlurBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                    final long start = System.currentTimeMillis();

                    mScriptBlur.setInput(mAllocationIn);
                    mScriptBlur.forEach(mAllocationOut);
                    mAllocationOut.copyTo(bitmapOut);
                    final long stop = System.currentTimeMillis();

                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmapOut);
                        Toast.makeText(RenderScriptActivity.this, (stop - start) + "ms", Toast.LENGTH_LONG).show();

                    }
                    });
                    }
                }).start();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRenderScript.destroy();
    }
}
