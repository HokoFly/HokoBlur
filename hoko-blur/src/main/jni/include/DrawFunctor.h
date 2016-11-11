//
// Created by 橡皮 on 16/11/9.
//
#include <android/log.h>
#include "Functor.h"
#ifndef DYNAMICBLUR_DRAWFUNCTOR_H
#define DYNAMICBLUR_DRAWFUNCTOR_H


namespace  android {

    class DrawFunctor : public Functor
    {
        public:
            DrawFunctor(jobject weakReferFunctor) {
                mWeakReferFunctor = weakReferFunctor;
            }
            ~DrawFunctor() {}



            status_t operator ()(int mode, void* info);


    private:
            jobject mWeakReferFunctor;
            void operate(int mode, void *info);


    };

}
#endif //DYNAMICBLUR_DRAWFUNCTOR_H
