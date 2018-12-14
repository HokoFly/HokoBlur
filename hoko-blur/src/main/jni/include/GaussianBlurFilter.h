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

JNIEXPORT void JNICALL Java_com_hoko_blur_filter_NativeBlurFilter_nativeGaussianBlur
        (JNIEnv *, jclass, jobject, jint, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif //HOKO_BLUR_GAUSSIANBLURFILTER_H
