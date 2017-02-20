#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "BlurUtil.h"
#include <android/log.h>
#include <android/bitmap.h>

#ifndef DYNAMICBLUR_STACKBLURFILTER_H
#define DYNAMICBLUR_STACKBLURFILTER_H

#ifdef __cplusplus
extern "C" {
#endif

#define max(a, b) ((a)>(b)?(a):(b))
#define min(a, b) ((a)<(b)?(a):(b))

JNIEXPORT void JNICALL Java_com_hoko_blurlibrary_util_NativeBlurHelper_nativeStackBlur
        (JNIEnv *, jobject, jobject, jint, jint, jint, jint);

void doHorizontalBlur(jint *, jint , jint , jint, jint, jint, jint, jint);
void doVerticalBlur(jint *, jint , jint , jint, jint, jint, jint, jint);
#ifdef __cplusplus
}
#endif
#endif
