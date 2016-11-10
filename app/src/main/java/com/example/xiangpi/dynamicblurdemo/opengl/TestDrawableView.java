package com.example.xiangpi.dynamicblurdemo.opengl;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.hoko.blurlibrary.functor.DrawFunctor;

/**
 * Created by xiangpi on 16/11/9.
 */
public class TestDrawableView extends View{
    private DrawFunctor mDrawFunctor;

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
        mDrawFunctor = new DrawFunctor(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawFunctor.doDraw(canvas);
    }
}
