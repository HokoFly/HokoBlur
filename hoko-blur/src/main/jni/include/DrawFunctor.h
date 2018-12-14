//
// Created by yuxfzju on 16/11/9.
//
#ifndef HOKO_BLUR_DRAWFUNCTOR_H
#define HOKO_BLUR_DRAWFUNCTOR_H

#include <android/log.h>
#include "Functor.h"

namespace android {

    class DrawFunctor : public Functor {
    public:
        jobject mWeakRefFunctor;

        DrawFunctor();

        ~DrawFunctor();

        status_t operator()(int mode, void *info);


    private:
        void operate(int mode, void *info);

    };

}
#endif //HOKO_BLUR_DRAWFUNCTOR_H
