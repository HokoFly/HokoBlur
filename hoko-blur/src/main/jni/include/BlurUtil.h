//
// Created by yuxfzju on 2017/2/14.
//

#ifndef HOKO_BLUR_BITMAPREPLACE_H
#define HOKO_BLUR_BITMAPREPLACE_H

#include <jni.h>
#include "android/bitmap.h"
#include "android/log.h"

#ifdef __cplusplus
extern "C" {
#endif

enum Direction{
    HORIZONTAL,
    VERTICAL,
    BOTH
};

jint clamp(jint i, jint minValue, jint maxValue);

JNIEXPORT void JNICALL
        Java_com_hoko_blur_util_BitmapUtil_replaceBitmap(JNIEnv *env, jclass type, jobject bitmap,
                                                         jintArray j_inArray, jint, jint, jint, jint);

#ifdef __cplusplus
}
#endif

#endif //HOKO_BLUR_BITMAPREPLACE_H
