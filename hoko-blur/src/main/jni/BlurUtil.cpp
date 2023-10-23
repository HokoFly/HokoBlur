//
// Created by yuxfzju on 2017/2/14.
//

#include "include/BlurUtil.h"

#ifdef __cplusplus
extern "C" {
#endif

jint clamp(jint i, jint minValue, jint maxValue) {
    if (i < minValue) {
        return minValue;
    } else if (i > maxValue) {
        return maxValue;
    } else {
        return i;
    }
}


JNIEXPORT void JNICALL
Java_com_hoko_blur_util_BitmapUtil_replaceBitmap(JNIEnv *env, jclass type, jobject jbitmap,
                                                 jintArray j_inArray, jint j_x, jint j_y,
                                                 jint j_deltaW, jint j_deltaH) {
    if (jbitmap == nullptr) {
        return;
    }
    AndroidBitmapInfo bmpInfo = {0};
    if (AndroidBitmap_getInfo(env, jbitmap, &bmpInfo) < 0) {
        return;
    }
    int *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, jbitmap, (void **) &pixels) < 0) {
        return;
    }
    jint *c_inArray;
    c_inArray = env->GetIntArrayElements(j_inArray, nullptr);
    int w = bmpInfo.width;
    for (int i = j_x; i < j_x + j_deltaW; i++) {
        for (int j = j_y; j < j_y + j_deltaH; j++) {
            jint argb = c_inArray[i - j_x + (j - j_y) * j_deltaW];
            jint a = ((argb >> 24) & 0xff) << 24;
            jint r = (argb >> 16) & 0xff;
            jint g = ((argb >> 8) & 0xff) << 8;
            jint b = (argb & 0xff) << 16;
            pixels[i + j * w] = a + r + g + b;
        }
    }
    AndroidBitmap_unlockPixels(env, jbitmap);
    env->ReleaseIntArrayElements(j_inArray, c_inArray, 0);
}

#ifdef __cplusplus
}
#endif