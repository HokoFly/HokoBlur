package com.hoko.blur.anno;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hoko.blur.HokoBlur.SCHEME_JAVA;
import static com.hoko.blur.HokoBlur.SCHEME_NATIVE;
import static com.hoko.blur.HokoBlur.SCHEME_OPENGL;

/**
 * Created by yuxfzju on 2017/2/9.
 */

@IntDef({SCHEME_OPENGL, SCHEME_NATIVE, SCHEME_JAVA})
@Retention(RetentionPolicy.SOURCE)
public @interface Scheme {
}
