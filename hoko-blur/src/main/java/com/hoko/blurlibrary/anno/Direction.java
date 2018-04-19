package com.hoko.blurlibrary.anno;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.hoko.blurlibrary.Blur.BOTH;
import static com.hoko.blurlibrary.Blur.HORIZONTAL;
import static com.hoko.blurlibrary.Blur.VERTICAL;

/**
 * Created by yuxfzju on 2017/2/20.
 */

@IntDef({HORIZONTAL, VERTICAL, BOTH})
@Retention(RetentionPolicy.SOURCE)
public @interface Direction {
}
