package com.example.xiangpi.dynamicblurdemo.opengl;

/**
 * Created by xiangpi on 16/8/29.
 */
public interface GLRenderer {

    void onDrawFrame();

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

}
