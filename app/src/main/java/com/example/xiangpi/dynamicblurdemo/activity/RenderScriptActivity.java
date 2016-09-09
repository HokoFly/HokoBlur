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

public class RenderScriptActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int BLUR_KERNEL_RADIUS = 10;

    private RenderScript mRenderScript;

    private Bitmap mBitmapIn;
    private Bitmap mBitmapOut;

    private Allocation mAllocationIn;
    private Allocation mAllocationOut;

    private ScriptC_invert mScriptInvert;
    private ScriptIntrinsicBlur mScriptBlur;
    private ScriptC_boxblur mScriptBoxBlur;
    private ScriptC_stackblur mScriptStackBlur;

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
        mBitmapIn = BitmapFactory.decodeResource(getResources(), ImageUtils.testImageRes);
        mBitmapOut = Bitmap.createBitmap(mBitmapIn.getWidth(), mBitmapIn.getHeight(), Bitmap.Config.ARGB_8888);

        mImageView.setImageBitmap(mBitmapIn);
        mAllocationIn = Allocation.createFromBitmap(mRenderScript, mBitmapIn);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, mBitmapIn);

        mScriptInvert = new ScriptC_invert(mRenderScript);

        mScriptBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        mScriptBlur.setRadius(BLUR_KERNEL_RADIUS);

        mScriptBoxBlur = new ScriptC_boxblur(mRenderScript);

        mScriptStackBlur = new ScriptC_stackblur(mRenderScript);

        mInvertBtn.setOnClickListener(this);
        mBoxBlurBtn.setOnClickListener(this);
        mStackBlurBtn.setOnClickListener(this);
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
                final long start;

                switch (view.getId()) {
                    case R.id.invert_btn:
                        start = System.currentTimeMillis();
                        mScriptInvert.forEach_invert(mAllocationIn, mAllocationOut);
                        break;

                    case R.id.box_blur_btn:
                        mScriptBoxBlur.set_input(mAllocationIn);
                        mScriptBoxBlur.set_output(mAllocationOut);
                        mScriptBoxBlur.set_radius(BLUR_KERNEL_RADIUS);
                        mScriptBoxBlur.set_width(mBitmapIn.getWidth());
                        mScriptBoxBlur.set_height(mBitmapIn.getHeight());
                        start = System.currentTimeMillis();
                        mScriptBoxBlur.forEach_boxblur(mAllocationIn);

                        break;
                    case R.id.stack_blur_btn:
                        mScriptStackBlur.set_input(mAllocationIn);
                        mScriptStackBlur.set_output(mAllocationOut);
                        mScriptStackBlur.set_width(mBitmapIn.getWidth());
                        mScriptStackBlur.set_height(mBitmapIn.getHeight());
                        mScriptStackBlur.set_radius(BLUR_KERNEL_RADIUS);

                        int[] rowIndices = new int[mBitmapIn.getHeight()];
                        int[] colIndices = new int[mBitmapIn.getWidth()];

                        for (int i = 0; i < mBitmapIn.getHeight(); i++) {
                            rowIndices[i] = i;
                        }

                        for (int i = 0; i < mBitmapIn.getWidth(); i++) {
                            colIndices[i] = i;
                        }

                        Allocation rows = Allocation.createSized(mRenderScript, Element.U32(mRenderScript), mBitmapIn.getHeight(), Allocation.USAGE_SCRIPT);
                        Allocation cols = Allocation.createSized(mRenderScript, Element.U32(mRenderScript), mBitmapIn.getWidth(), Allocation.USAGE_SCRIPT);

                        rows.copyFrom(rowIndices);
                        cols.copyFrom(colIndices);
                        start = System.currentTimeMillis();
                        mScriptStackBlur.forEach_blur_h(rows);
                        mScriptStackBlur.forEach_blur_v(cols);

                        break;
                    case R.id.gaussian_blur_btn:
                        mScriptBlur.setInput(mAllocationIn);
                        start = System.currentTimeMillis();
                        mScriptBlur.forEach(mAllocationOut);
                        break;
                    default:
                        start = System.currentTimeMillis();
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
