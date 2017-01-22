package com.example.xiangpi.dynamicblurdemo.opengl;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mBlurDrawable);
        } else {
            setBackgroundDrawable(mBlurDrawable);
        }
    }

}
