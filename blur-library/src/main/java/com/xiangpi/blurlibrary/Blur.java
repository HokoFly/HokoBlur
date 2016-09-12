package com.xiangpi.blurlibrary;

import android.content.Context;
import android.util.Log;

import com.xiangpi.blurlibrary.generator.IBlur;
import com.xiangpi.blurlibrary.generator.NativeBlurGenerator;
import com.xiangpi.blurlibrary.generator.OpenGLBlurGenerator;
import com.xiangpi.blurlibrary.generator.OriginBlurGenerator;
import com.xiangpi.blurlibrary.generator.RenderScriptBlurGenerator;

/**
 * Created by xiangpi on 16/9/7.
 */
public class Blur {

    // 模糊算法，模糊实现方式，模糊半径，尺寸缩放，输入图像

    public enum BlurMode {
        BOX, GAUSSIAN, STACK
    }

    public enum BlurScheme {
        RENDER_SCRIPT, OPENGL, NATIVE, JAVA
    }

    private static final BlurMode DEFAULT_MODE = BlurMode.GAUSSIAN;
    private static final BlurScheme DEFAULT_SCHEME = BlurScheme.RENDER_SCRIPT;
    private static final int DEFAULT_BLUR_RADIUS = 5;
    private static final float DEFAULT_SAMPLE_FACTOR = 5.0f;

    private static volatile Blur sHelper;

    private Context mCtx;

    private BlurMode mBlurMode = DEFAULT_MODE;
    private BlurScheme mBlurScheme = DEFAULT_SCHEME;
    private int mRadius = DEFAULT_BLUR_RADIUS;
    private float mSampleFactor = DEFAULT_SAMPLE_FACTOR;


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

    public Blur mode(BlurMode mode) {
        mBlurMode = mode;
        return sHelper;
    }

    public Blur scheme(BlurScheme scheme) {
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

    /**
     * 创建不同的模糊发生器
     * @return
     */
    public IBlur getBlurGenerator() {
        long start = System.currentTimeMillis();


        IBlur generator = null;

        if (mBlurScheme == BlurScheme.RENDER_SCRIPT) {
            generator = new RenderScriptBlurGenerator(mCtx);
        } else if (mBlurScheme == BlurScheme.OPENGL) {
            generator = new OpenGLBlurGenerator();
        } else if (mBlurScheme == BlurScheme.NATIVE){
            generator = new NativeBlurGenerator();
        } else if (mBlurScheme == BlurScheme.JAVA) {
            generator = new OriginBlurGenerator();
        }

        if (generator != null) {
            generator.setBlurMode(mBlurMode);
            generator.setBlurRadius(mRadius);
            generator.setSampleFactor(mSampleFactor);
        }
        long stop = System.currentTimeMillis();
        Log.d("generate duration", stop - start + "ms");

        return generator;

    }


}
