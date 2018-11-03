#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "BlurUtil.h"

#ifndef HOKO_BLUR_BOXBLURFILTER_H
#define HOKO_BLUR_BOXBLURFILTER_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_hoko_blur_filter_NativeBlurFilter_nativeBoxBlur
        (JNIEnv *, jclass , jobject, jint, jint, jint, jint);

void boxBlurHorizontal(int[], int[], int width, int height, int radius, int, int, int, int);
void boxBlurVertical(int[], int[], int width, int height, int radius, int, int, int, int);

#ifdef __cplusplus
}
#endif
#endif
