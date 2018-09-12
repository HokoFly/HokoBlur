#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "BlurUtil.h"
#include <android/log.h>
#include <android/bitmap.h>

#ifndef HOKO_BLUR_STACKBLURFILTER_H
#define HOKO_BLUR_STACKBLURFILTER_H

#ifdef __cplusplus
extern "C" {
#endif

#define max(a, b) ((a)>(b)?(a):(b))
#define min(a, b) ((a)<(b)?(a):(b))

JNIEXPORT void JNICALL Java_com_hoko_blur_filter_NativeBlurFilter_nativeStackBlur
        (JNIEnv *, jobject, jobject, jint, jint, jint, jint);

void doHorizontalBlur(jint *, jint , jint , jint, jint, jint, jint, jint);
void doVerticalBlur(jint *, jint , jint , jint, jint, jint, jint, jint);
#ifdef __cplusplus
}
#endif
#endif
