//
// Created by yuxfzju on 16/9/10.
//

#ifndef HOKO_BLUR_GAUSSIANBLURFILTER_H
#define HOKO_BLUR_GAUSSIANBLURFILTER_H


#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "math.h"
#include "BlurUtil.h"
#include <android/bitmap.h>
#include <android/log.h>


#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_hoko_blurlibrary_filter_NativeBlurFilter_nativeGaussianBlur
        (JNIEnv *, jobject, jobject, jint, jint, jint, jint);

float * makeKernel(jint r);
void gaussianBlurHorizontal(float *, jint *, jint *, jint, jint, jint, jint, jint, jint, jint);
void gaussianBlurVertical(float *, jint *, jint *, jint, jint, jint, jint, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif //HOKO_BLUR_GAUSSIANBLURFILTER_H
