package com.hoko.blur.anno;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hoko.blur.HokoBlur.MODE_BOX;
import static com.hoko.blur.HokoBlur.MODE_GAUSSIAN;
import static com.hoko.blur.HokoBlur.MODE_STACK;

/**
 * Created by yuxfzju on 2017/2/9.
 */

@IntDef({MODE_BOX, MODE_GAUSSIAN, MODE_STACK})
@Retention(RetentionPolicy.SOURCE)
public @interface Mode {
}