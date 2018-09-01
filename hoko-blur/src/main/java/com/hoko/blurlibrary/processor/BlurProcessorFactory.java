package com.hoko.blurlibrary.processor;

import com.hoko.blurlibrary.HokoBlur;
import com.hoko.blurlibrary.anno.Scheme;

class BlurProcessorFactory {

    static BlurProcessor get(@Scheme int scheme, BlurProcessor.Builder builder) {

        BlurProcessor generator = null;

        switch (scheme) {
            case HokoBlur.SCHEME_RENDER_SCRIPT:
                generator = new RenderScriptBlurProcessor(builder);
                break;
            case HokoBlur.SCHEME_OPENGL:
                generator = new OpenGLBlurProcessor(builder);
                break;
            case HokoBlur.SCHEME_NATIVE:
                generator = new NativeBlurProcessor(builder);
                break;
            case HokoBlur.SCHEME_JAVA:
                generator = new OriginBlurProcessor(builder);
                break;
            default:
                throw new IllegalArgumentException("Unsupported blur scheme!");
        }

        return generator;
    }
}
