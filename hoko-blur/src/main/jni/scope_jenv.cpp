//
// Created by yuxfzju on 2017/3/17.
//

#include "include/scope_jenv.h"
#include <stddef.h>
#include <unistd.h>
#include <pthread.h>

extern pthread_key_t g_env_key;

ScopeJEnv::ScopeJEnv(JavaVM* jvm, jint _capacity)
        : vm_(jvm), env_(NULL), we_attach_(false), status_(0) {
    do {
        env_ = (JNIEnv*)pthread_getspecific(g_env_key);

        if (NULL != env_) {
            break;
        }

        status_ = vm_->GetEnv((void**) &env_, JNI_VERSION_1_6);

        if (JNI_OK == status_) {
            break;
        }

        JavaVMAttachArgs args;
        args.group = NULL;
        args.name = "default";
        args.version = JNI_VERSION_1_6;

#ifdef ANDROID
        status_ = vm_->AttachCurrentThread(&env_, &args);
#else
        status_ = vm_->AttachCurrentThread((void **) &env_, &args);
#endif

        if (JNI_OK == status_) {
            we_attach_ = true;
            pthread_setspecific(g_env_key, env_);
        } else {
            env_ = NULL;
            return;
        }
    } while (false);

    jint ret = env_->PushLocalFrame(_capacity);
}

ScopeJEnv::~ScopeJEnv() {
    if (NULL != env_) {
        env_->PopLocalFrame(NULL);
    }
}

JNIEnv* ScopeJEnv::GetEnv() {
    return env_;
}

int ScopeJEnv::Status() {
    return status_;
}