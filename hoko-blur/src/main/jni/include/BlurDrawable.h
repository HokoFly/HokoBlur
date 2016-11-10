//
// Created by 橡皮 on 16/11/9.
//
#include <jni.h>

#ifndef DYNAMICBLUR_BLURDRAWABLE_H
#define DYNAMICBLUR_BLURDRAWABLE_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
        Java_com_hoko_blurlibrary_functor_DrawFunctor_createNativeFunctor(JNIEnv *env, jobject clazz,
                                                                          jobject functor);
#ifdef __cplusplus
}
#endif

#endif //DYNAMICBLUR_BLURDRAWABLE_H

