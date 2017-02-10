package com.hoko.blurlibrary.api;


/**
 * Created by xiangpi on 2017/2/10.
 */
public interface IRenderer<T> {
    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame(T t);

    /**
     * 释放OpenGL相关资源
     */
    void free();

}
