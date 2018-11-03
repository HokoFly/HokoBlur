//
// Created by yuxfzju on 16/11/9.
//

#include "include/DrawFunctor.h"
#include "include/BlurDrawable.h"
#include "ScopeJEnv.h"

using namespace android;

extern "C" void postEventFromNativeC(int, void *, jobject);

status_t DrawFunctor::operator()(int mode, void *info) {
    operate(mode, info);
    return 0;
}


void DrawFunctor::operate(int mode, void *info) {

    postEventFromNativeC(mode, info, mWeakRefFunctor);

    return;

}

DrawFunctor::DrawFunctor() {
}

DrawFunctor::~DrawFunctor() {
    ScopeJEnv scope_jenv(g_VM);
    JNIEnv *env = scope_jenv.GetEnv();
    if (env) {
        env->DeleteGlobalRef(mWeakRefFunctor);
    }
}

