//
// Created by 橡皮 on 16/11/9.
//
#include <android/log.h>
#include "Functor.h"

#ifndef DYNAMICBLUR_DRAWFUNCTOR_H
#define DYNAMICBLUR_DRAWFUNCTOR_H


namespace android {

    class DrawFunctor : public Functor {
    public:
        jobject mWeakRefFunctor;

        DrawFunctor() { }

        ~DrawFunctor() { }

        status_t operator()(int mode, void *info);


    private:
        void operate(int mode, void *info);


    };

}
#endif //DYNAMICBLUR_DRAWFUNCTOR_H
