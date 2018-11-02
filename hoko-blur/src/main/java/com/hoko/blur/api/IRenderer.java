package com.hoko.blur.api;


/**
 * Created by yuxfzju on 2017/2/10.
 */
public interface IRenderer<T> {
    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame(T t);

    void free();

}
