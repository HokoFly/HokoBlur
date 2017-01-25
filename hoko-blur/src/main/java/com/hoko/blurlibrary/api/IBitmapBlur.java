package com.hoko.blurlibrary.api;

import android.graphics.Bitmap;

import com.hoko.blurlibrary.api.IBlur;

/**
 * Created by xiangpi on 16/9/8.
 */
public interface IBitmapBlur extends IBlur {
    /**
     * 模糊操作
     * @param inBitmap 输入的bitmap
     * @return
     */
    Bitmap doBlur(Bitmap inBitmap);

}
