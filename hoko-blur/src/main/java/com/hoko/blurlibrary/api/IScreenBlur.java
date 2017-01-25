package com.hoko.blurlibrary.api;

import com.hoko.blurlibrary.opengl.functor.DrawFunctor;

/**
 * Created by xiangpi on 2017/1/23.
 */

public interface IScreenBlur extends IBlur {
    /**
     * info为模糊区域与屏幕的相对位置信息
     *
     * @param info
     */
    void doBlur(DrawFunctor.GLInfo info);

    void free();

}
