package com.hoko.blurlibrary.anno;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hoko.blurlibrary.Blur.MODE_BOX;
import static com.hoko.blurlibrary.Blur.MODE_GAUSSIAN;
import static com.hoko.blurlibrary.Blur.MODE_STACK;

/**
 * Created by xiangpi on 2017/2/9.
 */

@IntDef({MODE_BOX, MODE_GAUSSIAN, MODE_STACK})
@Retention(RetentionPolicy.SOURCE)
public @interface Mode {}