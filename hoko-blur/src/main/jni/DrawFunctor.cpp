//
// Created by 橡皮 on 16/11/9.
//

#include "include/DrawFunctor.h"
#include <android/log.h>



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

