package com.hoko.blur;

import android.content.Context;

import com.hoko.blur.api.IBlurBuild;
import com.hoko.blur.processor.HokoBlurBuild;

/**
 * Created by yuxfzju on 16/9/7.
 */
public class HokoBlur {

    public static final int MODE_BOX = 0;
    public static final int MODE_GAUSSIAN = 1;
    public static final int MODE_STACK = 2;

    public static final int SCHEME_RENDER_SCRIPT = 1001;
    public static final int SCHEME_OPENGL = 1002;
    public static final int SCHEME_NATIVE = 1003;
    public static final int SCHEME_JAVA = 1004;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int BOTH = 2;

    public static IBlurBuild with(Context context) {
        return new HokoBlurBuild(context.getApplicationContext());
    }

}
