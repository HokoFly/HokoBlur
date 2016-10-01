package com.hoko.blurlibrary.opengl.offscreen;

import android.graphics.Bitmap;

/**
 * Created by xiangpi on 16/8/29.
 */
public interface GLRenderer {

    void onDrawFrame();

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    Bitmap getInputBitmap();

}
