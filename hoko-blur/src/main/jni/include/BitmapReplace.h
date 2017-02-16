//
// Created by 橡皮 on 2017/2/14.
//

#ifndef DYNAMICBLUR_BITMAPREPLACE_H
#define DYNAMICBLUR_BITMAPREPLACE_H

#include <jni.h>
#include "android/bitmap.h"
#include "android/log.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_hoko_blurlibrary_util_BitmapUtil_replaceBitmap(JNIEnv *env, jobject instance,
                                                                      jobject bitmap, jintArray j_inArray);

#ifdef __cplusplus
}
#endif

#endif //DYNAMICBLUR_BITMAPREPLACE_H
