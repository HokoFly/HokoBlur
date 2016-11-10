//
// Created by 橡皮 on 16/11/9.
//

#include "include/BlurDrawable.h"
#include "include/DrawFunctor.h"

using namespace android;

JNIEXPORT jlong JNICALL
Java_com_hoko_blurlibrary_functor_DrawFunctor_createNativeFunctor(JNIEnv *env, jobject clazz,
                                                                  jobject functor) {
    DrawFunctor* drawFunctor = new DrawFunctor(env, functor);

    return (jlong) drawFunctor;

}