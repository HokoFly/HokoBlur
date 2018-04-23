package com.hoko.blurlibrary.anno;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hoko.blurlibrary.HokoBlur.BOTH;
import static com.hoko.blurlibrary.HokoBlur.HORIZONTAL;
import static com.hoko.blurlibrary.HokoBlur.VERTICAL;

/**
 * Created by yuxfzju on 2017/2/20.
 */

@IntDef({HORIZONTAL, VERTICAL, BOTH})
@Retention(RetentionPolicy.SOURCE)
public @interface Direction {
}
