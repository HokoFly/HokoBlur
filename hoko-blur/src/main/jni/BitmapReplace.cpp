//
// Created by 橡皮 on 2017/2/14.
//

#include <android/bitmap.h>
#include "include/BitmapReplace.h"


JNIEXPORT void JNICALL
    Java_com_hoko_blurlibrary_util_BitmapUtil_replaceBitmap(JNIEnv *env, jobject instance, jobject jbitmap, jintArray j_inArray) {

    if (jbitmap == NULL) {
        return;
    }

    AndroidBitmapInfo bmpInfo={0};
    if (AndroidBitmap_getInfo(env, jbitmap, &bmpInfo) < 0) {
        return;
    }

    int * pixels = NULL;
    if (AndroidBitmap_lockPixels(env, jbitmap, (void **)&pixels)) {
        return;
    }


    jint *c_inArray;
    c_inArray = env->GetIntArrayElements(j_inArray, NULL);

    int w = bmpInfo.width;
    int h = bmpInfo.height;

    for(int i = 0; i < w * h; i ++) {
        jint argb = c_inArray[i];
        jint a = ((argb >> 24) & 0xff) << 24;
        jint r = (argb >> 16) & 0xff;
        jint g = ((argb >> 8) & 0xff) << 8;
        jint b = (argb & 0xff) << 16;

        pixels[i] = a + r + g + b;

    }
//    __android_log_print(ANDROID_LOG_ERROR, "unlockPixels", "unlockPixels");

    AndroidBitmap_unlockPixels(env, jbitmap);

    env->ReleaseIntArrayElements(j_inArray, c_inArray, 0);

}