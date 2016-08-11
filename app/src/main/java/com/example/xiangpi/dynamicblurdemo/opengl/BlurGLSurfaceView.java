package com.example.xiangpi.dynamicblurdemo.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by xiangpi on 16/8/10.
 */
public class BlurGLSurfaceView extends GLSurfaceView {

    private BitmapRender mRender;

    public BlurGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public BlurGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRender = new BitmapRender(getContext());
        setRenderer(mRender);

//        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


}
