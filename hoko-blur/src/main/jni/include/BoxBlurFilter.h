#ifndef HOKO_BLUR_BOXBLURFILTER_H
#define HOKO_BLUR_BOXBLURFILTER_H

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "BlurUtil.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_hoko_blur_filter_NativeBlurFilter_nativeBoxBlur
        (JNIEnv *, jclass, jobject, jint, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
