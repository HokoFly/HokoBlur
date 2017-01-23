package com.hoko.blurlibrary.opengl.functor;

import com.hoko.blurlibrary.generator.IBlur;

/**
 * Created by xiangpi on 2017/1/23.
 */

public interface IScreenBlur extends IBlur {
    /**
     * info为模糊区域与屏幕的相对位置信息
     * @param info
     */
    void doBlur(DrawFunctor.GLInfo info);
}
