//
// Created by yuxfzju on 16/9/10.
//

#include "include/GaussianBlurFilter.h"

void JNICALL Java_com_hoko_blurlibrary_filter_NativeBlurFilter_nativeGaussianBlur
        (JNIEnv *env, jobject j_object, jobject jbitmap, jint j_radius, jint j_cores, jint j_index, jint j_direction) {

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

    int w = bmpInfo.width;
    int h = bmpInfo.height;

    float *kernel = NULL;
    kernel = makeKernel(j_radius);

    jint *copy = NULL;
    copy = (jint *) malloc(sizeof(jint) * w * h);

    for (int i = 0; i < w * h; i++) {
        copy[i] = pixels[i];
    }

    if (j_direction == HORIZONTAL) {
        int deltaY = h / j_cores;
        int startY = j_index * deltaY;

        if (j_index == j_cores - 1) {
            deltaY = h - (j_cores - 1) * deltaY;
        }

        gaussianBlurHorizontal(kernel, copy, pixels, w, h, j_radius, 0, startY, w, deltaY);

    } else if (j_direction == VERTICAL){
        int deltaX = w / j_cores;
        int startX = j_index * deltaX;

        if (j_index == j_cores - 1) {
            deltaX = w - (j_cores - 1) * (w / j_cores);
        }

        gaussianBlurVertical(kernel, copy, pixels, w, h, j_radius, startX, 0, deltaX, h);
    }

    AndroidBitmap_unlockPixels(env, jbitmap);

    free(copy);
    free(kernel);
}

void gaussianBlurHorizontal(float *kernel, jint *inPixels, jint *outPixels, jint width, jint height, jint radius,
                            jint startX, jint startY, jint deltaX, jint deltaY) {
    jint cols = 2 * radius + 1;
    jint cols2 = cols / 2;
    jint x, y, col;

    jint endY = startY + deltaY;
    jint endX = startX + deltaX;

    for (y = startY; y < endY; y++) {
        jint ioffset = y * width;
        for (x = startX; x < endX; x++) {
            float r = 0, g = 0, b = 0;
            int moffset = cols2;
            for (col = -cols2; col <= cols2; col++) {
                float f = kernel[moffset + col];

                if (f != 0) {
                    jint ix = x + col;
                    if (ix < startX) {
                        ix = startX;
                    } else if (ix >= endX) {
                        ix = endX - 1;
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
void gaussianBlurVertical(float *kernel, jint *inPixels, jint *outPixels, jint width, jint height, jint radius,
                          jint startX, jint startY, jint deltaX, jint deltaY) {
    jint cols = 2 * radius + 1;
    jint cols2 = cols / 2;
    jint x, y, col;

    jint endY = startY + deltaY;
    jint endX = startX + deltaX;

    for (x = startX; x < endX; x++) {
        jint ioffset = x;
        for (y = startY; y < endY; y++) {
            float r = 0, g = 0, b = 0;
            int moffset = cols2;
            for (col = -cols2; col <= cols2; col++) {
                float f = kernel[moffset + col];

                if (f != 0) {
                    jint iy = y + col;
                    if (iy < startY) {
                        iy = startY;
                    } else if (iy >= endY) {
                        iy = endY - 1;
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
    float total = 0;
    jint index = 0;
    for (row = -r; row <= r; row++) {
        matrix[index] = exp(-1 * (row * row) / sigma22) / sigma;
        total += matrix[index];
        index++;
    }
    for (i = 0; i < rows; i++) {
        matrix[i] /= total;
    }

    return matrix;
}

