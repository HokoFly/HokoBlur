//
// Created by yuxfzju on 2017/2/14.
//

#include "include/BlurUtil.h"

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
    Java_com_hoko_blur_util_BitmapUtil_replaceBitmap(JNIEnv *env, jobject instance, jobject jbitmap, jintArray j_inArray, jint j_x, jint j_y, jint j_deltaW, jint j_deltaH) {

    if (jbitmap == NULL) {
        return;
    }

    AndroidBitmapInfo bmpInfo={0};
    if (AndroidBitmap_getInfo(env, jbitmap, &bmpInfo) < 0) {
        return;
    }

    int * pixels = NULL;
    if (AndroidBitmap_lockPixels(env, jbitmap, (void **)&pixels) < 0) {
        return;
    }


    jint *c_inArray;
    c_inArray = env->GetIntArrayElements(j_inArray, NULL);

    int w = bmpInfo.width;
    int h = bmpInfo.height;

    for (int i = j_x; i < j_x + j_deltaW; i++) {
        for (int j = j_y; j < j_y + j_deltaH; j++) {
            jint argb = c_inArray[i + j * h];
            jint a = ((argb >> 24) & 0xff) << 24;
            jint r = (argb >> 16) & 0xff;
            jint g = ((argb >> 8) & 0xff) << 8;
            jint b = (argb & 0xff) << 16;
            pixels[i + j * h] = a + r + g + b;

        }
    }
//    for(int i = j_y * w; i < w * (j_y + j_deltaH); i ++) {
//        jint argb = c_inArray[i];
//        jint a = ((argb >> 24) & 0xff) << 24;
//        jint r = (argb >> 16) & 0xff;
//        jint g = ((argb >> 8) & 0xff) << 8;
//        jint b = (argb & 0xff) << 16;
//
//        pixels[i] = a + r + g + b;
//
//    }
//    __android_log_print(ANDROID_LOG_ERROR, "unlockPixels", "unlockPixels");

    AndroidBitmap_unlockPixels(env, jbitmap);

    env->ReleaseIntArrayElements(j_inArray, c_inArray, 0);

}