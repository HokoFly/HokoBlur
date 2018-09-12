package com.hoko.blur.anno;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hoko.blur.HokoBlur.BOTH;
import static com.hoko.blur.HokoBlur.HORIZONTAL;
import static com.hoko.blur.HokoBlur.VERTICAL;

/**
 * Created by yuxfzju on 2017/2/20.
 */

@IntDef({HORIZONTAL, VERTICAL, BOTH})
@Retention(RetentionPolicy.SOURCE)
public @interface Direction {
}
