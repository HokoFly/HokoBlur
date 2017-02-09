package com.hoko.blurlibrary.anno;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hoko.blurlibrary.Blur.SCHEME_JAVA;
import static com.hoko.blurlibrary.Blur.SCHEME_NATIVE;
import static com.hoko.blurlibrary.Blur.SCHEME_OPENGL;
import static com.hoko.blurlibrary.Blur.SCHEME_RENDER_SCRIPT;

/**
 * Created by xiangpi on 2017/2/9.
 */

@IntDef({SCHEME_RENDER_SCRIPT, SCHEME_OPENGL, SCHEME_NATIVE, SCHEME_JAVA})
@Retention(RetentionPolicy.SOURCE)
public @interface Scheme {}
