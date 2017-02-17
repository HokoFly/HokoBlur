//
// Created by 橡皮 on 16/9/10.
//

#include "include/GaussianBlurFilter.h"
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "math.h"
#include "include/BoxBlurFilter.h"
#include <android/log.h>


void JNICALL Java_com_hoko_blurlibrary_generator_NativeBlurGenerator_nativeGaussianBlur
        (JNIEnv *env, jobject j_object, jintArray j_inArray, jint j_w, jint j_h, jint j_radius) {

    jint *c_inArray;
    jint *c_outArray;
    jint arr_len;
    float *c_kernelArray;

    c_inArray = env->GetIntArrayElements(j_inArray, NULL);
    if (c_inArray == NULL) {
        return;
    }

    arr_len = env->GetArrayLength(j_inArray);

    c_outArray = (jint *) malloc(sizeof(jint) * arr_len);

    c_kernelArray = makeKernel(j_radius);

    gaussianBlurHorizontal(c_kernelArray, c_inArray, c_outArray, j_w, j_h, j_radius);

    gaussianBlurVertical(c_kernelArray, c_outArray, c_inArray, j_w, j_h, j_radius);

    env->SetIntArrayRegion(j_inArray, 0, arr_len, c_inArray);

    env->ReleaseIntArrayElements(j_inArray, c_inArray, 0);
    free(c_outArray);
    free(c_kernelArray);
}

void gaussianBlurHorizontal(float *kernel, jint *inPixels, jint *outPixels, jint width, jint height,
                            jint radius) {
    jint cols = 2 * radius + 1;
    jint cols2 = cols / 2;
    jint x, y, col;

    for (y = 0; y < height; y++) {
        jint ioffset = y * width;
        for (x = 0; x < width; x++) {
            float r = 0, g = 0, b = 0;
            int moffset = cols2;
            for (col = -cols2; col <= cols2; col++) {
                float f = kernel[moffset + col];

                if (f != 0) {
                    jint ix = x + col;
                    if (ix < 0) {
                        ix = 0;
                    } else if (ix >= width) {
                        ix = width - 1;
                    }
                    jint rgb = inPixels[ioffset + ix];
                    r += f * ((rgb >> 16) & 0xff);
                    g += f * ((rgb >> 8) & 0xff);
                    b += f * (rgb & 0xff);
                }
            }

            jint outIndex = ioffset + x;
            jint ia = (inPixels[ioffset + x] >> 24) & 0xff;
            jint ir = clamp((jint) (r + 0.5), 0, 255);
            jint ig = clamp((jint) (g + 0.5), 0, 255);
            jint ib = clamp((jint) (b + 0.5), 0, 255);
            outPixels[outIndex] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
        }
    }
}
void gaussianBlurVertical(float *kernel, jint *inPixels, jint *outPixels, jint width, jint height,
                            jint radius) {
    jint cols = 2 * radius + 1;
    jint cols2 = cols / 2;
    jint x, y, col;

    for (x = 0; x < width; x++) {
        jint ioffset = x;
        for (y = 0; y < height; y++) {
            float r = 0, g = 0, b = 0;
            int moffset = cols2;
            for (col = -cols2; col <= cols2; col++) {
                float f = kernel[moffset + col];

                if (f != 0) {
                    jint iy = y + col;
                    if (iy < 0) {
                        iy = 0;
                    } else if (iy >= height) {
                        iy = height - 1;
                    }
                    jint rgb = inPixels[ioffset + iy * width];
                    r += f * ((rgb >> 16) & 0xff);
                    g += f * ((rgb >> 8) & 0xff);
                    b += f * (rgb & 0xff);
                }
            }
            jint outIndex = ioffset + y * width;
            jint ia = (inPixels[ioffset + x] >> 24) & 0xff;
            jint ir = clamp((jint) (r + 0.5), 0, 255);
            jint ig = clamp((jint) (g + 0.5), 0, 255);
            jint ib = clamp((jint) (b + 0.5), 0, 255);
            outPixels[outIndex] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
        }
    }
}

float *makeKernel(jint r) {
    jint i, row;
    jint rows = r * 2 + 1;
    float *matrix = (float *) malloc(sizeof(float) * rows);
    float sigma = (r + 1) / 2.0f;
    float sigma22 = 2 * sigma * sigma;
    float sigmaPi2 = (float) (2 * M_PI * sigma);
    float sqrtSigmaPi2 = (float) sqrt(sigmaPi2);
    float radius2 = r * r;
    float total = 0;
    jint index = 0;
    for (row = -r; row <= r; row++) {
        float distance = row * row;
        if (distance > radius2) {
            matrix[index] = 0;
        } else {
            matrix[index] = (float) exp(-(distance) / sigma22) / sqrtSigmaPi2;
        }
        total += matrix[index];
        index++;
    }
    for (i = 0; i < rows; i++) {
        matrix[i] /= total;
    }

    return matrix;
}

