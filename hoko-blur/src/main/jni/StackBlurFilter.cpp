//
// Created by 橡皮 on 16/7/28.
//

#include "include/StackBlurFilter.h"
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>


JNIEXPORT void JNICALL Java_com_hoko_blurlibrary_generator_NativeBlurGenerator_nativeStackBlur
        (JNIEnv *env, jobject j_object, jintArray j_inArray, jint j_w, jint j_h, jint j_radius) {

    jint *c_inArray;
    jint arr_len;

    c_inArray = env->GetIntArrayElements(j_inArray, NULL);
    if (c_inArray == NULL) {
        return;
    }

    arr_len = env->GetArrayLength(j_inArray);

    doInnerBlur(c_inArray, j_w, j_h, j_radius);

    env->SetIntArrayRegion(j_inArray, 0, arr_len, c_inArray);
    env->ReleaseIntArrayElements(j_inArray, c_inArray, 0);
}

void doInnerBlur(jint *pix, jint w, jint h, jint radius) {

    jint wm = w - 1;
    jint hm = h - 1;
    jint wh = w * h;
    jint div = radius + radius + 1;

    short *r;
    short *g;
    short *b;

    r = (short *) malloc(sizeof(short) * wh);
    g = (short *) malloc(sizeof(short) * wh);
    b = (short *) malloc(sizeof(short) * wh);

    jint rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
    jint *vmin;

    vmin = (jint *) malloc(sizeof(jint) * max(w, h));

    jint divsum = (div + 1) >> 1;
    divsum *= divsum;

    short *dv;
    dv = (short *) malloc(sizeof(short) * 256 * divsum);

    for (i = 0; i < 256 * divsum; i++) {
        dv[i] = (short) (i / divsum);
    }

    yw = yi = 0;

    //jint stack[div][3];

    jint (*stack)[3];
    stack = (jint(*)[3]) malloc(sizeof(jint) * div * 3);

    jint stackpointer;
    jint stackstart;
    jint *sir;
    jint rbs;
    jint r1 = radius + 1;
    jint routsum, goutsum, boutsum;
    jint rinsum, ginsum, binsum;

    for (y = 0; y < h; y++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        for (i = -radius; i <= radius; i++) {
            p = pix[yi + min(wm, max(i, 0))];
            sir = stack[i + radius];
            sir[0] = (p & 0xff0000) >> 16;
            sir[1] = (p & 0x00ff00) >> 8;
            sir[2] = (p & 0x0000ff);
            rbs = r1 - abs(i);
            rsum += sir[0] * rbs;
            gsum += sir[1] * rbs;
            bsum += sir[2] * rbs;
            if (i > 0) {
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
            } else {
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
            }
        }
        stackpointer = radius;

        for (x = 0; x < w; x++) {

            r[yi] = dv[rsum];
            g[yi] = dv[gsum];
            b[yi] = dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            sir = stack[stackstart % div];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];

            if (y == 0) {
                vmin[x] = min(x + radius + 1, wm);
            }
            p = pix[yw + vmin[x]];

            sir[0] = (p & 0xff0000) >> 16;
            sir[1] = (p & 0x00ff00) >> 8;
            sir[2] = (p & 0x0000ff);

            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            sir = stack[(stackpointer) % div];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];

            yi++;
        }
        yw += w;
    }

    for (x = 0; x < w; x++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        yp = -radius * w;
        for (i = -radius; i <= radius; i++) {
            yi = max(0, yp) + x;

            sir = stack[i + radius];

            sir[0] = r[yi];
            sir[1] = g[yi];
            sir[2] = b[yi];

            rbs = r1 - abs(i);

            rsum += r[yi] * rbs;
            gsum += g[yi] * rbs;
            bsum += b[yi] * rbs;

            if (i > 0) {
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
            } else {
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
            }

            if (i < hm) {
                yp += w;
            }
        }
        yi = x;
        stackpointer = radius;
        for (y = 0; y < h; y++) {
            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            sir = stack[stackstart % div];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];

            if (x == 0) {
                vmin[y] = min(y + r1, hm) * w;
            }
            p = x + vmin[y];

            sir[0] = r[p];
            sir[1] = g[p];
            sir[2] = b[p];

            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            sir = stack[stackpointer];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];

            yi += w;
        }
    }

    free(r);
    free(g);
    free(b);
    free(dv);
    free(stack);
}
