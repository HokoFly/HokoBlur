package com.hoko.blurlibrary.util;

/**
 * Created by xiangpi on 16/9/4.
 */
public class KernelUtil {

    private static final float GAUSSIAN_SIGMA = 10.0f;

//    public static float[] getGaussianKernel(int radius) throws IllegalArgumentException {
//        if (radius <= 0) {
//            throw new IllegalArgumentException("The radius of Kernel must be > 0");
//        }
//
//        final int d = 2 * radius + 1;
//        final int size = d * d;
//
//        float[] kernel = new float[size];
//
//        float sum = 0;
//
//        for (int i = -radius; i <= radius; i++) {
//            for (int j = radius; j >= -radius; j--) {
//                sum += kernel[d * (radius - j) + i + radius] = (float) (1f / (2 * Math.PI * Math.pow(GAUSSIAN_SIGMA, 2)) *
//                                        Math.exp(- (i * i + j * j) / (2 *  Math.pow(GAUSSIAN_SIGMA, 2))));
//            }
//        }
//
//        for (int i = 0; i < size; i++) {
//            kernel[i] /= sum;
//        }
//
//        return kernel;
//    }

    public static float[] getGaussianKernel(int radius) throws IllegalArgumentException {
        if (radius <= 0) {
            throw new IllegalArgumentException("The radius of Kernel must be > 0");
        }

        final int d = 2 * radius + 1;

        float[] kernel = new float[d];

        float sum = 0;

        for (int i = -radius; i <= radius; i++) {
                sum += kernel[i + radius] = (float) (1f / Math.sqrt(2 * Math.PI * Math.pow(GAUSSIAN_SIGMA, 2)) *
                                        Math.exp(- (i * i) / (2 *  Math.pow(GAUSSIAN_SIGMA, 2))));
        }

        for (int i = 0; i < d; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }


        public static float[] getBoxKernel(int radius) throws IllegalArgumentException {
        if (radius <= 0) {
            throw new IllegalArgumentException("The radius of Kernel must be > 0");
        }

        final int d = 2 * radius + 1;
        final int size = d * d;

        float[] kernel = new float[size];

        float sum = 0;

        for (int i = -radius; i <= radius; i++) {
            for (int j = radius; j >= -radius; j--) {
                kernel[d * (radius - j) + i + radius] = 1f / size;
            }
        }

        return kernel;
    }

}
