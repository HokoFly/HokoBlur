//
// Created by 橡皮 on 16/11/9.
//

#include "include/BlurDrawable.h"

using namespace android;
using namespace uirenderer;


JNIEXPORT jlong JNICALL
Java_com_hoko_blurlibrary_opengl_functor_DrawFunctor_createNativeFunctor(JNIEnv *env, jobject clazz,
                                                                  jobject weakRefFunctor) {
    DrawFunctor *drawFunctor = new DrawFunctor();
    drawFunctor->mWeakRefFunctor = env->NewGlobalRef(weakRefFunctor);

    env->DeleteLocalRef(weakRefFunctor);

    return (jlong) drawFunctor;

}

void postEventFromNativeC(int mode, void *info, jobject functor) {

    if (mFunctorClazz == NULL || mGlInfoClazz == NULL) {
        return;
    }

    JNIEnv *env = NULL;

    javaVM->GetEnv((void **) &env, JNI_VERSION_1_6);

    jmethodID mPostMethodID = env->GetStaticMethodID((jclass) mFunctorClazz, "postEventFromNative",
                                                     "(Ljava/lang/ref/WeakReference;Lcom/hoko/blurlibrary/opengl/functor/DrawFunctor$GLInfo;I)V");

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

    JNIEnv *env = NULL;

    javaVM->GetEnv((void **) &env, JNI_VERSION_1_6);

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
    javaVM = vm;
    JNIEnv *env = NULL;
    jclass functorCls = NULL;
    jclass glInfoCls = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // 查找要加载的本地方法Class引用
    functorCls = env->FindClass("com/hoko/blurlibrary/opengl/functor/DrawFunctor");
    glInfoCls = env->FindClass("com/hoko/blurlibrary/opengl/functor/DrawFunctor$GLInfo");

    if (functorCls == NULL || glInfoCls == NULL) {
        return JNI_ERR;
    }
    // 将class的引用缓存到全局变量中
    mFunctorClazz = reinterpret_cast<jclass>(env->NewGlobalRef(functorCls));
    mGlInfoClazz = reinterpret_cast<jclass>(env->NewGlobalRef(glInfoCls));

    env->DeleteLocalRef(functorCls);
    env->DeleteLocalRef(glInfoCls);

    return JNI_VERSION_1_6;
}

