//
// Created by 橡皮 on 16/11/9.
//
#include "Functor.h"

#ifndef DYNAMICBLUR_DRAWFUNCTOR_H
#define DYNAMICBLUR_DRAWFUNCTOR_H

namespace  android {

    class DrawFunctor : public Functor
    {
        public:
            DrawFunctor(JNIEnv *env, jobject weakReferFunctor) {
                mEnv = env;
                mWeakReferFunctor = weakReferFunctor;
            }
            ~DrawFunctor() {}

            status_t operator ()(int mode, void* info);

        private:
            jobject mWeakReferFunctor;
            JNIEnv *mEnv;
            void postEventFromNative(int mode, void* info);

    };

}





#endif //DYNAMICBLUR_DRAWFUNCTOR_H
