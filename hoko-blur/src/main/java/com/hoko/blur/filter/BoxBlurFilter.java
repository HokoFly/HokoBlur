package com.hoko.blur.filter;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.anno.Direction;

import static com.hoko.blur.util.MathUtil.clamp;

/**
 * Created by yuxfzju on 2016/7/24.
 */
final class BoxBlurFilter {

    static void doBlur(int[] in, int width, int height, int radius, @Direction int round) {
        int[] result = new int[width * height];

        if (round == HokoBlur.HORIZONTAL) {
            boxBlurHorizontal(in, result, width, height, radius);
            System.arraycopy(result, 0, in, 0, result.length);
        } else if (round == HokoBlur.VERTICAL) {
            boxBlurVertical(in, result, width, height, radius);
            System.arraycopy(result, 0, in, 0, result.length);

        } else {
            boxBlurHorizontal(in, result, width, height, radius);
            boxBlurVertical(result, in, width, height, radius);

        }
    }

    private static void boxBlurHorizontal(int[] in, int[] out, int width, int height, int radius) {
        int widthMinus1 = width - 1;
        int tableSize = 2 * radius + 1;
        int divide[] = new int[256 * tableSize];

        // construct a query table from 0 to 255
        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        //
        for (int y = 0; y < height; y++) {
            int ta = 0, tr = 0, tg = 0, tb = 0; // ARGB

            for (int i = -radius; i <= radius; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            int baseIndex = y * width;
            for (int x = 0; x < width; x++) {
                out[baseIndex + x] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + radius + 1;
                if (i1 > widthMinus1)
                    i1 = widthMinus1;
                int i2 = x - radius;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
//                outIndex += height;
            }
            inIndex += width;
        }
    }

    private static void boxBlurVertical(int[] in, int[] out, int width, int height, int radius) {
        int heightMinus1 = height - 1;
        int tableSize = 2 * radius + 1;
        int divide[] = new int[256 * tableSize];

        // construct a query table from 0 to 255
        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        for (int x = 0; x < width; x++) {
            int ta = 0, tr = 0, tg = 0, tb = 0; // ARGB

            for (int i = -radius; i <= radius; i++) {
                int rgb = in[x + clamp(i, 0, height - 1) * width];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int y = 0; y < height; y++) { // Sliding window computation
                out[y * width + x] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = y + radius + 1;
                if (i1 > heightMinus1)
                    i1 = heightMinus1;
                int i2 = y - radius;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = in[x + i1 * width];
                int rgb2 = in[x + i2 * width];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
            }
        }
    }


}