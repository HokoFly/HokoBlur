package com.hoko.blurlibrary.origin;

import static com.hoko.blurlibrary.util.BlurUtil.clamp;

/**
 * Created by xiangpi on 16/9/10.
 */
public class GaussianBlurFilter {

    public static void doBlur(int[] in, int width, int height, int radius) {

        int[] result = new int[width * height];
        float[] kernel = makeKernel(radius);

        gaussianBlurHorizontal(kernel, in, result, width, height);
        gaussianBlurHorizontal(kernel, result, in, height, width);
    }

    public static void gaussianBlurHorizontal(float[] kernel, int[] inPixels, int[] outPixels, int width, int height) {
        int cols = kernel.length;
        int cols2 = cols / 2;

        for (int y = 0; y < height; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0;
                int moffset = cols2;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = kernel[moffset + col];

                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            ix = 0;
                        } else if (ix >= width) {
                            ix = width - 1;
                        }
                        int rgb = inPixels[ioffset + ix];
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                int ia = (inPixels[ioffset + x] >> 24) & 0xff;
                int ir = clamp((int) (r + 0.5), 0, 255);
                int ig = clamp((int) (g + 0.5), 0, 255);
                int ib = clamp((int) (b + 0.5), 0, 255);
                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    /**
     * Make a Gaussian blur kernel.
     */
    private static float[] makeKernel(int r) {
        int rows = r * 2 + 1;
        float[] matrix = new float[rows];
        float sigma = (r + 1) / 2.0f;
        float sigma22 = 2 * sigma * sigma;
        float sigmaPi2 = (float) (2 * Math.PI * sigma);
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = r * r;
        float total = 0;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            float distance = row * row;
            if (distance > radius2)
                matrix[index] = 0;
            else
                matrix[index] = (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++)
            matrix[i] /= total;

        return matrix;
    }

}
