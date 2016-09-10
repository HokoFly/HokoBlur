//
// Created by 橡皮 on 16/9/10.
//

#ifndef DYNAMICBLUR_GAUSSIANBLURFILTER_H
#define DYNAMICBLUR_GAUSSIANBLURFILTER_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

float * makeKernel(jint r);
void gaussianBlurHorizontal(float *, jint *, jint *, jint, jint);

JNIEXPORT void JNICALL Java_com_xiangpi_blurlibrary_generator_NativeBlurGenerator_nativeGaussianBlur
        (JNIEnv *, jobject, jintArray, jint, jint, jint);


#ifdef __cplusplus
}
#endif
#endif //DYNAMICBLUR_GAUSSIANBLURFILTER_H
