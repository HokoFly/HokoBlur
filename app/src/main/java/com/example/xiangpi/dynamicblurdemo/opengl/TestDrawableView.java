package com.example.xiangpi.dynamicblurdemo.opengl;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.opengl.drawable.BlurDrawable;
import com.hoko.blurlibrary.opengl.functor.DrawFunctor;
import com.hoko.blurlibrary.opengl.texture.Texture;

/**
 * Created by xiangpi on 16/11/9.
 */
public class TestDrawableView extends View{
    private BlurDrawable mBlurDrawable;

    public TestDrawableView(Context context) {
        super(context);
        init();
    }

    public TestDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestDrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBlurDrawable = new BlurDrawable();
        mBlurDrawable.setBlurRadius(10);
        mBlurDrawable.setBlurMode(Blur.MODE_STACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mBlurDrawable);
        } else {
            setBackgroundDrawable(mBlurDrawable);
        }

        final ValueAnimator animator = ValueAnimator.ofInt(0, 10, 0);
        animator.setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int radius = (int) animation.getAnimatedValue();
                mBlurDrawable.setBlurRadius(radius);
            }
        });

//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                animator.start();
//            }
//        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBlurDrawable.destroy();
    }
}
