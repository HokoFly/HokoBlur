//
// Created by 橡皮 on 16/7/28.
//

#include "include/BoxBlur.h"
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>


JNIEXPORT void JNICALL Java_com_example_xiangpi_dynamicblurdemo_activity_BoxBlurActivity_nativeBoxBlur
        (JNIEnv * env, jobject j_object, jintArray j_inArray, jint j_w, jint j_h, jint j_radius) {

    jint *c_inArray;
    jint *c_outArray;
    jint arr_len;

    c_inArray = env->GetIntArrayElements(j_inArray, NULL);
    if (c_inArray == NULL) {
        return;
    }

    arr_len = env->GetArrayLength(j_inArray);

    c_outArray = (jint *) malloc(sizeof(jint) * arr_len);

    blurHorizontal(c_inArray, c_outArray, j_w, j_h, j_radius);
    blurHorizontal(c_outArray, c_inArray, j_h, j_w, j_radius);

    env->SetIntArrayRegion(j_inArray, 0, arr_len, c_inArray);

    env->ReleaseIntArrayElements(j_inArray, c_inArray, 0);
    free(c_outArray);
}

  void blurHorizontal(jint * in, jint * out, jint width, jint height, jint radius) {
        jint widthMinus1 = width-1;
        jint tableSize = 2*radius+1;
        jint divide[256*tableSize];

        // the value scope will be 0 to 255, and number of 0 is table size
        // will get means from index not calculate result again since
        // color value must be  between 0 and 255.
        for ( jint i = 0; i < 256*tableSize; i++ )
            divide[i] = i/tableSize;

        int inIndex = 0;

        //
        for ( jint y = 0; y < height; y++ ) {
            jint outIndex = y;
            jint ta = 0, tr = 0, tg = 0, tb = 0; // ARGB -> prepare for the alpha, red, green, blue color value.

            for ( jint i = -radius; i <= radius; i++ ) {
                jint rgb = in[inIndex + clamp(i, 0, width-1)]; // read input pixel data here. table size data.
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for ( jint x = 0; x < width; x++ ) { // get output pixel data.
                out[ outIndex ] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb]; // calculate the output data.

                jint i1 = x+radius+1;
                if ( i1 > widthMinus1 )
                    i1 = widthMinus1;
                jint i2 = x-radius;
                if ( i2 < 0 )
                    i2 = 0;
                jint rgb1 = in[inIndex+i1];
                jint rgb2 = in[inIndex+i2];

                ta += ((rgb1 >> 24) & 0xff)-((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000)-(rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00)-(rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff)-(rgb2 & 0xff);
                outIndex += height; // per column or per row as cycle...
            }
            inIndex += width; // next (i+ column number * n, n=1....n-1)
        }
  }

jint clamp(jint i, jint minValue, jint maxValue) {
    if (i < minValue) {
        return minValue;
    } else if (i > maxValue) {
        return maxValue;
    } else {
        return i;
    }
}