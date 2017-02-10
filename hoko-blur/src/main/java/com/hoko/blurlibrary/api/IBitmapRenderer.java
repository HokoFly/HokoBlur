package com.hoko.blurlibrary.api;

import android.graphics.Bitmap;

/**
 * Created by xiangpi on 16/8/29.
 */
public interface IBitmapRenderer {

    void onDrawFrame(Bitmap bitmap);

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

}
