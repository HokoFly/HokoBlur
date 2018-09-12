package com.example.hokoblurdemo.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.example.hokoblurdemo.R;
import com.hoko.blur.opengl.drawable.BlurDrawable;
import com.hoko.blur.view.BlurFrameLayout;

public class BlurDrawableActivity extends Activity {

    private BlurFrameLayout mFrameLayout;

    private ValueAnimator mAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur_drawable);

        mFrameLayout = (BlurFrameLayout) findViewById(R.id.blur_frameLayout);

        final BlurDrawable blurDrawable = new BlurDrawable();
        findViewById(R.id.test_view).setBackgroundDrawable(blurDrawable);

        mAnimator = ValueAnimator.ofInt(0, 20);
        mAnimator.setDuration(2000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFrameLayout.getBlurDrawable().radius((Integer) animation.getAnimatedValue());
            }
        });
    }

    public void remove(View view) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.height = lp.height - 10;
        lp.width = lp.height - 20;
        view.setLayoutParams(lp);
//        ((ViewGroup) view.getParent()).removeView(view);
    }

    public void animate(View view) {
       mAnimator.start();
    }
}
