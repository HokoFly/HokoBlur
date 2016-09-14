package com.example.xiangpi.dynamicblurdemo.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.xiangpi.dynamicblurdemo.R;
import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.generator.IBlur;

public class MultiBlurActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final float SAMPLE_FACTOR = 8.0f;
    private static final int INIT_RADIUS = 5;

    private static int[] testImageRes = {R.mipmap.sample1, R.mipmap.sample2, R.mipmap.sample3, R.mipmap.sample4, R.mipmap.sample5};

    private int mCurrentImageRes = testImageRes[0];

    private int index = 0;

    private Spinner mSchemeSpinner;
    private Spinner mModeSpinner;

    private SeekBar mSeekBar;

    private Button mResetBtn;
    private Button mAnimBtn;

    private TextView mRadiusText;

    private ImageView mImageView;

    private Blur mBlur;

    private IBlur mGenerator;

    private Bitmap mInBitmap;

    private BlurTask mLatestTask;

    private int mRadius = INIT_RADIUS;

    private ValueAnimator mAnimator;

    private boolean mIsBlurAnimating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_blur);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBlur = Blur.with(this).sampleFactor(SAMPLE_FACTOR);

        mImageView = (ImageView) findViewById(R.id.photo);
        mSchemeSpinner = (Spinner) findViewById(R.id.scheme_spinner);
        mModeSpinner = (Spinner) findViewById(R.id.mode_spinner);
        mSeekBar = (SeekBar) findViewById(R.id.radius_seekbar);
        mResetBtn = (Button) findViewById(R.id.reset_btn);
        mAnimBtn = (Button) findViewById(R.id.anim_btn);

        mRadiusText = (TextView) findViewById(R.id.blur_radius);

        ArrayAdapter<CharSequence> schemeAdapter = ArrayAdapter.createFromResource(this,
                R.array.blur_schemes, android.R.layout.simple_spinner_item);
        schemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this,
                R.array.blur_modes, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSchemeSpinner.setAdapter(schemeAdapter);
        mModeSpinner.setAdapter(modeAdapter);

        mSchemeSpinner.setOnItemSelectedListener(this);
        mModeSpinner.setOnItemSelectedListener(this);

        mSeekBar.setOnSeekBarChangeListener(this);

        mResetBtn.setOnClickListener(this);
        mAnimBtn.setOnClickListener(this);

        mImageView.setOnClickListener(this);

        setImage(mCurrentImageRes);

    }


    private void setImage(@DrawableRes final int id) {
        mImageView.setImageResource(id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mIsBlurAnimating = true;
                mInBitmap = BitmapFactory.decodeResource(getResources(), id);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAnimator != null && mAnimator.isRunning()) {
                            mAnimator.end();
                        }
                        mAnimator = ValueAnimator.ofInt(0, mRadius);
                        mAnimator.setInterpolator(new LinearInterpolator());
                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                updateImage((Integer) animation.getAnimatedValue());
                            }

                        });

                        mAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mIsBlurAnimating = false;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        mAnimator.setDuration(300);
                        mAnimator.start();
                    }
                });

            }
        }).start();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final int spinnerId = parent.getId();
        if (spinnerId == R.id.scheme_spinner) {
            switch (position) {
                case 0: mBlur.scheme(Blur.BlurScheme.RENDER_SCRIPT);
                    break;
                case 1: mBlur.scheme(Blur.BlurScheme.OPENGL);
                    break;
                case 2: mBlur.scheme(Blur.BlurScheme.NATIVE);
                    break;
                case 3: mBlur.scheme(Blur.BlurScheme.JAVA);
                    break;
            }

        } else if (spinnerId == R.id.mode_spinner) {
            switch (position) {
                case 0: mBlur.mode(Blur.BlurMode.GAUSSIAN);
                    break;
                case 1: mBlur.mode(Blur.BlurMode.STACK);
                    break;
                case 2: mBlur.mode(Blur.BlurMode.BOX);
                    break;
            }

        }

        mGenerator = mBlur.getBlurGenerator();
        mGenerator.setBlurRadius(mRadius);
        updateImage(mRadius);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.reset_btn:
                mImageView.setImageResource(mCurrentImageRes);
                mSeekBar.setProgress(0);
                break;
            case R.id.anim_btn:
                ValueAnimator animator = ValueAnimator.ofInt(0, 25, 0);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int r = (int) animation.getAnimatedValue();
                        updateImage(r);
                    }
                });
                animator.setDuration(1000);
                animator.start();
                break;
            case R.id.photo:
                if (!mIsBlurAnimating) {
                    mCurrentImageRes = testImageRes[++index % testImageRes.length];
                    setImage(mCurrentImageRes);
                }
                break;
        }



    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        final int radius = (int) (progress / 1000f * 25);
        mRadiusText.setText("模糊半径: " + radius);
        updateImage(radius);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void updateImage(int radius) {
        mRadius = radius;
        mSeekBar.setProgress((int) (mRadius / 25f * 1000));
        if (mLatestTask != null) {
            mLatestTask.cancel(false);
        }

        mLatestTask = new BlurTask();
        mLatestTask.execute(radius);
    }

    private class BlurTask extends AsyncTask<Integer, Void, Bitmap> {
        private boolean mIssued = false;

        @Override
        protected Bitmap doInBackground(Integer... params) {
            Bitmap output = null;

            if (!isCancelled()) {
                mIssued = true;
                int radius = params[0];
                if (mInBitmap != null && !mInBitmap.isRecycled()) {
                    mGenerator.setBlurRadius(radius);
                    long start = System.nanoTime();
                    output = mGenerator.doBlur(mInBitmap);
                    long stop = System.nanoTime();
                    Log.i("Total elapsed time", (stop - start) / 1000000f + "ms");
                }
            }

            return output;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            mImageView.setImageBitmap(bitmap);
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            if (mIssued && bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLatestTask != null) {
            mLatestTask.cancel(true);
        }
    }
}
