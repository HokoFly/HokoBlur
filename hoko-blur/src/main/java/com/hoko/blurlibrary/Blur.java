package com.hoko.blurlibrary;

import android.content.Context;
import android.support.annotation.IntDef;

import com.hoko.blurlibrary.api.IBitmapBlur;
import com.hoko.blurlibrary.generator.NativeBlurGenerator;
import com.hoko.blurlibrary.generator.OpenGLBlurGenerator;
import com.hoko.blurlibrary.generator.OriginBlurGenerator;
import com.hoko.blurlibrary.generator.RenderScriptBlurGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by xiangpi on 16/9/7.
 */
public class Blur {

    // 模糊算法，模糊实现方式，模糊半径，尺寸缩放，输入图像

    public static final int MODE_BOX = 0;
    public static final int MODE_GAUSSIAN = 1;
    public static final int MODE_STACK = 2;

    @IntDef({MODE_BOX, MODE_GAUSSIAN, MODE_STACK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BlurMode {}

    public static final int SCHEME_RENDER_SCRIPT = 1001;
    public static final int SCHEME_OPENGL = 1002;
    public static final int SCHEME_NATIVE = 1003;
    public static final int SCHEME_JAVA = 1004;

    @IntDef({SCHEME_RENDER_SCRIPT, SCHEME_OPENGL, SCHEME_NATIVE, SCHEME_JAVA})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BlurScheme {}

    private static final @BlurMode int DEFAULT_MODE = MODE_STACK;
    private static final @BlurScheme int DEFAULT_SCHEME = SCHEME_NATIVE;
    private static final int DEFAULT_BLUR_RADIUS = 5;
    private static final float DEFAULT_SAMPLE_FACTOR = 5.0f;
    private static final boolean DEFAULT_FORCE_COPY = false;

    private static volatile Blur sHelper;

    private Context mCtx;

    private @BlurMode int mMode = DEFAULT_MODE;
    private @BlurScheme int mBlurScheme = DEFAULT_SCHEME;
    private int mRadius = DEFAULT_BLUR_RADIUS;
    private float mSampleFactor = DEFAULT_SAMPLE_FACTOR;
    private boolean mIsForceCopy = DEFAULT_FORCE_COPY;

    private Blur(Context context) {
        mCtx = context.getApplicationContext();
    }

    public static Blur with(Context context) {
        if (sHelper == null) {
            synchronized (Blur.class) {
                if (sHelper == null) {
                    sHelper = new Blur(context);
                }
            }
        }

        return sHelper;
    }

    public Blur mode(@BlurMode int mode) {
        mMode = mode;
        return sHelper;
    }

    public Blur scheme(@BlurScheme int scheme) {
        mBlurScheme = scheme;
        return sHelper;
    }

    public Blur radius(int radius) {
        mRadius = radius;
        return sHelper;
    }

    public Blur sampleFactor(float factor) {
        mSampleFactor = factor;
        return sHelper;
    }

    public Blur forceCopy(boolean isForceCopy) {
        mIsForceCopy = isForceCopy;
        return sHelper;
    }

    /**
     * 创建不同的模糊发生器
     * @return
     */
    public IBitmapBlur blurGenerator() {
        IBitmapBlur generator = null;

        switch (mBlurScheme) {
            case Blur.SCHEME_RENDER_SCRIPT:
                generator = new RenderScriptBlurGenerator(mCtx);
                break;
            case Blur.SCHEME_OPENGL:
                generator = new OpenGLBlurGenerator();
                break;
            case Blur.SCHEME_NATIVE:
                generator = new NativeBlurGenerator();
                break;
            case Blur.SCHEME_JAVA:
                generator = new OriginBlurGenerator();
                break;

        }

        if (generator != null) {
            generator.setBlurMode(mMode);
            generator.setBlurRadius(mRadius);
            generator.setSampleFactor(mSampleFactor);
            generator.forceCopy(mIsForceCopy);
        }

        return generator;

    }

}
