package com.example.xiangpi.dynamicblurdemo.BlurOp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.example.xiangpi.dynamicblurdemo.util.ImageMath;

/**
 * Created by 余晓飞 on 2016/7/24.
 */
/*
 * The Box Blur code below is part the BlurTestAndroid project by patrickfav (https://github.com/patrickfav)
 *
 * https://github.com/patrickfav/BlurTestAndroid/blob/master/BlurBenchmark/src/main/java/at/favre/app/blurbenchmark/blur/algorithms/BoxBlur.java
 *
 * The box blur algorithm was implemented using this post http://stackoverflow.com/questions/8218438/android-box-blur-algorithm
 *
 */
public class BoxBlur {

    public static Bitmap blur(int radius, Bitmap bmp) {
        assert (radius & 1) == 0 : "Range must be odd.";

        Bitmap blurred = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(blurred);

        int w = bmp.getWidth();
        int h = bmp.getHeight();

        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);

        boxBlurHorizontal(pixels, w, h, radius / 2);
        boxBlurVertical(pixels, w, h, radius / 2);

        c.drawBitmap(pixels, 0, w, 0.0F, 0.0F, w, h, true, null);

        return blurred;
    }

    private static void boxBlurHorizontal(int[] pixels, int w, int h,
                                          int halfRange) {
        int index = 0;
        int[] newColors = new int[w];

        for (int y = 0; y < h; y++) {
            int hits = 0;
            long r = 0;
            long g = 0;
            long b = 0;
            for (int x = -halfRange; x < w; x++) {
                int oldPixel = x - halfRange - 1;
                if (oldPixel >= 0) {
                    int color = pixels[index + oldPixel];
                    if (color != 0) {


                        r -= (color >> 16) & 0xff;
                        g -= (color >> 8) & 0xff;
                        b -= (color & 0xff);
//
//
//                        r -= Color.red(color);
//                        g -= Color.green(color);
//                        b -= Color.blue(color);
                    }
                    hits--;
                }

                int newPixel = x + halfRange;
                if (newPixel < w) {
                    int color = pixels[index + newPixel];
                    if (color != 0) {
//                        r += Color.red(color);
//                        g += Color.green(color);
//                        b += Color.blue(color);
                        r += (color >> 16) & 0xff;
                        g += (color >> 8) & 0xff;
                        b += (color & 0xff);
                    }
                    hits++;
                }

                if (x >= 0) {
                    newColors[x] = Color.argb(0xFF, (int) (r / hits), (int) (g / hits), (int) (b / hits));
                }
            }

            System.arraycopy(newColors, 0, pixels, index + 0, w);

            index += w;
        }
    }

    private static void boxBlurVertical(int[] pixels, int w, int h,
                                        int halfRange) {

        int[] newColors = new int[h];
        int oldPixelOffset = -(halfRange + 1) * w;
        int newPixelOffset = (halfRange) * w;

        for (int x = 0; x < w; x++) {
            int hits = 0;
            long r = 0;
            long g = 0;
            long b = 0;
            int index = -halfRange * w + x;
            for (int y = -halfRange; y < h; y++) {
                int oldPixel = y - halfRange - 1;
                if (oldPixel >= 0) {
                    int color = pixels[index + oldPixelOffset];
                    if (color != 0) {
                        r -= (color >> 16) & 0xff;
                        g -= (color >> 8) & 0xff;
                        b -= (color & 0xff);
                    }
                    hits--;
                }

                int newPixel = y + halfRange;
                if (newPixel < h) {
                    int color = pixels[index + newPixelOffset];
                    if (color != 0) {
                        r += (color >> 16) & 0xff;
                        g += (color >> 8) & 0xff;
                        b += (color & 0xff);
                    }
                    hits++;
                }

                if (y >= 0) {
                    newColors[y] = Color.argb(0xFF, (int) (r / hits), (int) (g / hits), (int) (b / hits));
                }

                index += w;
            }

            for (int y = 0; y < h; y++) {
                pixels[y * w + x] = newColors[y];
            }
        }
    }


    public static void fastBlur(int[] in, int width, int height, int radius) {
        int[] result = new int[width * height];
        blurHorizontal(in, result, width, height, radius);
        blurHorizontal(result, in, height, width, radius);
    }
    public static void blurHorizontal( int[] in, int[] out, int width, int height, int radius ) {
        int widthMinus1 = width-1;
        int tableSize = 2*radius+1;
        int divide[] = new int[256*tableSize];

        // the value scope will be 0 to 255, and number of 0 is table size
        // will get means from index not calculate result again since
        // color value must be  between 0 and 255.
        for ( int i = 0; i < 256*tableSize; i++ )
            divide[i] = i/tableSize;

        int inIndex = 0;

        //
        for ( int y = 0; y < height; y++ ) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0; // ARGB -> prepare for the alpha, red, green, blue color value.

            for ( int i = -radius; i <= radius; i++ ) {
                int rgb = in[inIndex + ImageMath.clamp(i, 0, width-1)]; // read input pixel data here. table size data.
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for ( int x = 0; x < width; x++ ) { // get output pixel data.
                out[ outIndex ] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb]; // calculate the output data.

                int i1 = x+radius+1;
                if ( i1 > widthMinus1 )
                    i1 = widthMinus1;
                int i2 = x-radius;
                if ( i2 < 0 )
                    i2 = 0;
                int rgb1 = in[inIndex+i1];
                int rgb2 = in[inIndex+i2];

                ta += ((rgb1 >> 24) & 0xff)-((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000)-(rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00)-(rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff)-(rgb2 & 0xff);
                outIndex += height; // per column or per row as cycle...
            }
            inIndex += width; // next (i+ column number * n, n=1....n-1)
        }
    }


}