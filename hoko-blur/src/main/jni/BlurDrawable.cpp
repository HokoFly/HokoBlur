//
// Created by 橡皮 on 16/11/9.
//

#include <android/log.h>
#include "include/BlurDrawable.h"
#include "include/DrawFunctor.h"

using namespace android;

JNIEXPORT jlong JNICALL
Java_com_hoko_blurlibrary_functor_DrawFunctor_createNativeFunctor(JNIEnv *env, jobject clazz,
                                                                  jobject functor) {
    DrawFunctor* drawFunctor = new DrawFunctor(functor);

    return (jlong) drawFunctor;

}

void postEventFromNativeC(jobject weakRefFunctor, int mode, void *info) {

    if (weakRefFunctor != NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "functor", "functor is not null。%d", &weakRefFunctor);

    }
    JNIEnv* env = NULL;

    javaVM->GetEnv((void **)&env, JNI_VERSION_1_6);

    jmethodID mPostMethodID = env->GetStaticMethodID((jclass) mFunctorClazz, "postEventFromNative", "(Ljava/lang/ref/WeakReference;Lcom/hoko/blurlibrary/functor/DrawFunctor$GLInfo;I)V");

    env->CallStaticVoidMethod((jclass) mFunctorClazz, mPostMethodID, NULL, NULL, 9);

}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    javaVM = vm;
    JNIEnv* env = NULL;
    jclass cls = NULL;
    if(vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // 查找要加载的本地方法Class引用
    cls = env->FindClass("com/hoko/blurlibrary/functor/DrawFunctor");
    if(cls == NULL) {
        return JNI_ERR;
    }
    // 将class的引用缓存到全局变量中
    mFunctorClazz = env->NewWeakGlobalRef(cls);

    env->DeleteLocalRef(cls);

    return JNI_VERSION_1_6;
}

