package com.example.hokoblurdemo.activity;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hokoblurdemo.R;
import com.hoko.blur.HokoBlur;
import com.hoko.blur.api.IBlurBuild;
import com.hoko.blur.processor.BlurProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiBlurActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final float SAMPLE_FACTOR = 8.0f;
    private static final int INIT_RADIUS = 5;

    private static final int[] TEST_IMAGE_RES = {R.drawable.sample1, R.drawable.sample2};

    private int mCurrentImageRes = TEST_IMAGE_RES[0];
    private int index = 0;
    private SeekBar mSeekBar;
    private TextView mRadiusText;
    private ImageView mImageView;

    private IBlurBuild mBlurBuilder;
    private BlurProcessor mProcessor;
    private Bitmap mInBitmap;
    private int mRadius = INIT_RADIUS;
    private ValueAnimator mAnimator;
    private ValueAnimator mRoundAnimator;
    private volatile Future mFuture;
    private ExecutorService mDispatcher = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_blur);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mImageView = findViewById(R.id.photo);
        mImageView.setOnClickListener(this);
        mSeekBar = findViewById(R.id.radius_seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mRadiusText = findViewById(R.id.blur_radius);

        Spinner schemeSpinner = findViewById(R.id.scheme_spinner);
        schemeSpinner.setAdapter(makeSpinnerAdapter(R.array.blur_schemes));
        schemeSpinner.setOnItemSelectedListener(this);

        Spinner modeSpinner = findViewById(R.id.mode_spinner);
        modeSpinner.setAdapter(makeSpinnerAdapter(R.array.blur_modes));
        modeSpinner.setOnItemSelectedListener(this);

        Button resetBtn = findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(this);
        Button animBtn = findViewById(R.id.anim_btn);
        animBtn.setOnClickListener(this);

        setImage(mCurrentImageRes);
        mBlurBuilder = HokoBlur.with(this).sampleFactor(SAMPLE_FACTOR);

    }

    private SpinnerAdapter makeSpinnerAdapter(@ArrayRes int arrayRes) {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                arrayRes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerAdapter;
    }


    private void setImage(@DrawableRes final int id) {
        mImageView.setImageResource(id);
        mDispatcher.submit(new Runnable() {
            @Override
            public void run() {
                mInBitmap = BitmapFactory.decodeResource(getResources(), id);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endAnimators();
                        mAnimator = ValueAnimator.ofInt(0, (int) (mRadius / 25f * 1000));
                        mAnimator.setInterpolator(new LinearInterpolator());
                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                mSeekBar.setProgress((Integer) animation.getAnimatedValue());
                                updateImage((int) ((Integer) animation.getAnimatedValue() / 1000f * 25f));
                            }

                        });

                        mAnimator.setDuration(300);
                        mAnimator.start();
                    }
                });
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final int spinnerId = parent.getId();
        if (spinnerId == R.id.scheme_spinner) {
            switch (position) {
                case 0:
                    mBlurBuilder.scheme(HokoBlur.SCHEME_RENDER_SCRIPT);
                    break;
                case 1:
                    mBlurBuilder.scheme(HokoBlur.SCHEME_OPENGL);
                    break;
                case 2:
                    mBlurBuilder.scheme(HokoBlur.SCHEME_NATIVE);
                    break;
                case 3:
                    mBlurBuilder.scheme(HokoBlur.SCHEME_JAVA);
                    break;
            }

        } else if (spinnerId == R.id.mode_spinner) {
            switch (position) {
                case 0:
                    mBlurBuilder.mode(HokoBlur.MODE_GAUSSIAN);
                    break;
                case 1:
                    mBlurBuilder.mode(HokoBlur.MODE_STACK);
                    break;
                case 2:
                    mBlurBuilder.mode(HokoBlur.MODE_BOX);
                    break;
            }

        }
        endAnimators();
        mProcessor = mBlurBuilder.processor();
        mProcessor.radius(mRadius);
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
                endAnimators();
                mImageView.setImageResource(mCurrentImageRes);
                mSeekBar.setProgress(0);
                break;
            case R.id.anim_btn:
                endAnimators();
                mRoundAnimator = ValueAnimator.ofInt(0, 1000, 0);
                mRoundAnimator.setInterpolator(new LinearInterpolator());
                mRoundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mSeekBar.setProgress((int) animation.getAnimatedValue());
                        final int radius = (int) ((int) animation.getAnimatedValue() / 1000f * 25);
                        updateImage(radius);
                    }
                });
                mRoundAnimator.setDuration(2000);
                mRoundAnimator.start();
                break;
            case R.id.photo:
                mCurrentImageRes = TEST_IMAGE_RES[++index % TEST_IMAGE_RES.length];
                setImage(mCurrentImageRes);
                break;
        }


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        final int radius = (int) (progress / 1000f * 25);
        mRadiusText.setText("Blur Radius: " + radius);
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
        cancelPreTask();
        mFuture = mDispatcher.submit(new BlurTask(mInBitmap, mProcessor, radius) {
            @Override
            void onBlurSuccess(final Bitmap bitmap) {
                if (!isFinishing() && bitmap != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        });
    }

    private void cancelPreTask() {
        if (mFuture != null && !mFuture.isCancelled() && !mFuture.isDone()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    private void endAnimators() {
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.end();
        }
        if (mRoundAnimator != null && mRoundAnimator.isStarted()) {
            mRoundAnimator.end();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPreTask();
    }

    private abstract static class BlurTask implements Runnable {
        private Bitmap bitmap;
        private BlurProcessor blurProcessor;
        private int radius;

        BlurTask(Bitmap bitmap, BlurProcessor blurProcessor, int radius) {
            this.bitmap = bitmap;
            this.blurProcessor = blurProcessor;
            this.radius = radius;
        }

        @Override
        public void run() {
            if (bitmap != null && !bitmap.isRecycled() && blurProcessor != null) {
                blurProcessor.radius(radius);
                onBlurSuccess(blurProcessor.blur(bitmap));
            }
        }

        abstract void onBlurSuccess(Bitmap bitmap);
    }
}
