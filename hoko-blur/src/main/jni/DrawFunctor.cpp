//
// Created by 橡皮 on 16/11/9.
//

#include "include/DrawFunctor.h"
#include <android/log.h>


using namespace android;

status_t DrawFunctor::operator ()(int mode, void* info) {
    postEventFromNative(mode, info);
    return 0;
}

void DrawFunctor::postEventFromNative(int mode, void* info)
{
    jclass clazz = (*mEnv).FindClass("com/hoko/blurlibrary/functor/DrawFunctor");
    if (clazz == NULL) {
        return;
    }

    jmethodID postEventMethod = (*mEnv).GetStaticMethodID(clazz, "postEventFromNative", "(Ljava/lang/ref/WeakReference;Lcom/hoko/blurlibrary/functor/DrawFunctor/GLInfo;I)V");
    if (postEventMethod == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "functor", "找不到postEventFromNative这个静态方法。");
        return;
    }

    (*mEnv).CallStaticVoidMethod(clazz, postEventMethod, mWeakReferFunctor, info, mode);

    (*mEnv).DeleteLocalRef(clazz);

}

