//
// Created by yuxfzju on 16/11/9.
//
#include "include/BlurDrawable.h"
#include "ScopeJEnv.h"


using namespace android;
using namespace uirenderer;

JavaVM *g_VM;
pthread_key_t g_env_key;
jclass mFunctorClazz;
jclass mGlInfoClazz;

static void __DetachCurrentThread(void *a) {
    if (NULL != g_VM) {
        g_VM->DetachCurrentThread();
    }
}


JNIEXPORT jlong JNICALL
Java_com_hoko_blur_opengl_functor_DrawFunctor_createNativeFunctor(JNIEnv *env, jobject clazz,
                                                                  jobject weakRefFunctor) {
    DrawFunctor *drawFunctor = new DrawFunctor();
    drawFunctor->mWeakRefFunctor = env->NewGlobalRef(weakRefFunctor);

    env->DeleteLocalRef(weakRefFunctor);

    return (jlong) drawFunctor;

}

JNIEXPORT void JNICALL
Java_com_hoko_blur_opengl_functor_DrawFunctor_releaseFunctor(JNIEnv *env, jobject clazz,
                                                             jlong j_functor_ptr) {
    DrawFunctor *drawFunctor = (DrawFunctor *) j_functor_ptr;

    delete drawFunctor;
}

void postEventFromNativeC(int mode, void *info, jobject functor) {

    if (mFunctorClazz == NULL || mGlInfoClazz == NULL) {
        return;
    }

    ScopeJEnv scope_jenv(g_VM);
    JNIEnv *env = scope_jenv.GetEnv();

    jmethodID mPostMethodID = env->GetStaticMethodID((jclass) mFunctorClazz, "postEventFromNative",
                                                     "(Ljava/lang/ref/WeakReference;Lcom/hoko/blur/opengl/functor/DrawFunctor$GLInfo;I)V");

    jmethodID infoConstructID = env->GetMethodID(mGlInfoClazz, "<init>", "()V");

    jobject jDrawGlInfo = env->NewObject(mGlInfoClazz, infoConstructID);

    DrawGlInfo *c_drawGlInfo = reinterpret_cast<DrawGlInfo *>(info);

    copyGlInfo(&jDrawGlInfo, c_drawGlInfo);

    env->CallStaticVoidMethod((jclass) mFunctorClazz, mPostMethodID, functor, jDrawGlInfo, mode);

    env->DeleteLocalRef(jDrawGlInfo);

}

jobject *copyGlInfo(jobject *j_info, DrawGlInfo *c_info) {
    if (j_info == NULL || c_info == NULL) {
        return NULL;
    }

    ScopeJEnv scope_jenv(g_VM);
    JNIEnv *env = scope_jenv.GetEnv();

    jfieldID clipLeftFieldId = env->GetFieldID(mGlInfoClazz, "clipLeft", "I");
    jfieldID clipTopFieldId = env->GetFieldID(mGlInfoClazz, "clipTop", "I");
    jfieldID clipRightFieldId = env->GetFieldID(mGlInfoClazz, "clipRight", "I");
    jfieldID clipBottomFieldId = env->GetFieldID(mGlInfoClazz, "clipBottom", "I");
    jfieldID widthFieldId = env->GetFieldID(mGlInfoClazz, "viewportWidth", "I");
    jfieldID heightFieldId = env->GetFieldID(mGlInfoClazz, "viewportHeight", "I");
    jfieldID isLayerFieldId = env->GetFieldID(mGlInfoClazz, "isLayer", "Z");
    jfieldID transformFieldId = env->GetFieldID(mGlInfoClazz, "transform", "[F");

    env->SetIntField(*j_info, clipLeftFieldId, c_info->clipLeft);
    env->SetIntField(*j_info, clipTopFieldId, c_info->clipTop);
    env->SetIntField(*j_info, clipRightFieldId, c_info->clipRight);
    env->SetIntField(*j_info, clipBottomFieldId, c_info->clipBottom);
    env->SetIntField(*j_info, widthFieldId, c_info->width);
    env->SetIntField(*j_info, heightFieldId, c_info->height);
    env->SetBooleanField(*j_info, isLayerFieldId, c_info->isLayer);

    jobject jTransform = env->GetObjectField(*j_info, transformFieldId);
    int len = sizeof(c_info->transform) / sizeof(float);
    env->SetFloatArrayRegion((jfloatArray) jTransform, 0, len, c_info->transform);
    env->DeleteLocalRef(jTransform);
    return j_info;

}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_VM = vm;
    JNIEnv *env = NULL;
    jclass functorCls = NULL;
    jclass glInfoCls = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    if (0 != pthread_key_create(&g_env_key, __DetachCurrentThread)) {
        __android_log_print(ANDROID_LOG_ERROR, "pthread", "create g_env_key fail");
    }

    // find functor class
    functorCls = env->FindClass("com/hoko/blur/opengl/functor/DrawFunctor");
    glInfoCls = env->FindClass("com/hoko/blur/opengl/functor/DrawFunctor$GLInfo");

    if (functorCls == NULL || glInfoCls == NULL) {
        return JNI_ERR;
    }
    // cache global reference
    mFunctorClazz = reinterpret_cast<jclass>(env->NewGlobalRef(functorCls));
    mGlInfoClazz = reinterpret_cast<jclass>(env->NewGlobalRef(glInfoCls));

    env->DeleteLocalRef(functorCls);
    env->DeleteLocalRef(glInfoCls);

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        //LOGE("GetEnv failed!");
        return;
    }

    if (mFunctorClazz != NULL && env != NULL) {
        env->DeleteGlobalRef(mFunctorClazz);
        mFunctorClazz = NULL;
    }
    if (mGlInfoClazz != NULL && env != NULL) {
        env->DeleteGlobalRef(mGlInfoClazz);
        mGlInfoClazz = NULL;
    }

    g_VM = NULL;

}
