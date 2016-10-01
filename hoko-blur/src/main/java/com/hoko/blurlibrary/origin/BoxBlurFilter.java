package com.hoko.blurlibrary.origin;

/**
 * Created by 余晓飞 on 2016/7/24.
 */
public class BoxBlurFilter {

    public static void doBlur(int[] in, int width, int height, int radius) {
        int[] result = new int[width * height];
        boxBlurHorizontal(in, result, width, height, radius);
        boxBlurHorizontal(result, in, height, width, radius);
    }

    private static void boxBlurHorizontal(int[] in, int[] out, int width, int height, int radius) {
        int widthMinus1 = width - 1;
        int tableSize = 2 * radius + 1;
        int divide[] = new int[256 * tableSize];

        // 建立 0 到 255的查询表
        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        //
        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0; // ARGB

            for (int i = -radius; i <= radius; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) { // 滑动窗口的方式运算.
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

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
                outIndex += height;
            }
            inIndex += width;
        }
    }
//
//    public static Bitmap boxBlur(int radius, Bitmap bmp) {
//        assert (radius & 1) == 0 : "Range must be odd.";
//
//        Bitmap blurred = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas c = new Canvas(blurred);
//
//        int w = bmp.getWidth();
//        int h = bmp.getHeight();
//
//        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
//        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
//
//        boxBlurHorizontal(pixels, w, h, radius / 2);
//        boxBlurVertical(pixels, w, h, radius / 2);
//
//        c.drawBitmap(pixels, 0, w, 0.0F, 0.0F, w, h, true, null);
//
//        return blurred;
//    }
//
//    private static void boxBlurHorizontal(int[] pixels, int w, int h,
//                                          int halfRange) {
//        int index = 0;
//        int[] newColors = new int[w];
//
//        for (int y = 0; y < h; y++) {
//            int hits = 0;
//            long r = 0;
//            long g = 0;
//            long b = 0;
//            for (int x = -halfRange; x < w; x++) {
//                int oldPixel = x - halfRange - 1;
//                if (oldPixel >= 0) {
//                    int color = pixels[index + oldPixel];
//                    if (color != 0) {
//
//
//                        r -= (color >> 16) & 0xff;
//                        g -= (color >> 8) & 0xff;
//                        b -= (color & 0xff);
////
////
////                        r -= Color.red(color);
////                        g -= Color.green(color);
////                        b -= Color.blue(color);
//                    }
//                    hits--;
//                }
//
//                int newPixel = x + halfRange;
//                if (newPixel < w) {
//                    int color = pixels[index + newPixel];
//                    if (color != 0) {
////                        r += Color.red(color);
////                        g += Color.green(color);
////                        b += Color.blue(color);
//                        r += (color >> 16) & 0xff;
//                        g += (color >> 8) & 0xff;
//                        b += (color & 0xff);
//                    }
//                    hits++;
//                }
//
//                if (x >= 0) {
//                    newColors[x] = Color.argb(0xFF, (int) (r / hits), (int) (g / hits), (int) (b / hits));
//                }
//            }
//
//            System.arraycopy(newColors, 0, pixels, index + 0, w);
//
//            index += w;
//        }
//    }
//
//    private static void boxBlurVertical(int[] pixels, int w, int h,
//                                        int halfRange) {
//
//        int[] newColors = new int[h];
//        int oldPixelOffset = -(halfRange + 1) * w;
//        int newPixelOffset = (halfRange) * w;
//
//        for (int x = 0; x < w; x++) {
//            int hits = 0;
//            long r = 0;
//            long g = 0;
//            long b = 0;
//            int index = -halfRange * w + x;
//            for (int y = -halfRange; y < h; y++) {
//                int oldPixel = y - halfRange - 1;
//                if (oldPixel >= 0) {
//                    int color = pixels[index + oldPixelOffset];
//                    if (color != 0) {
//                        r -= (color >> 16) & 0xff;
//                        g -= (color >> 8) & 0xff;
//                        b -= (color & 0xff);
//                    }
//                    hits--;
//                }
//
//                int newPixel = y + halfRange;
//                if (newPixel < h) {
//                    int color = pixels[index + newPixelOffset];
//                    if (color != 0) {
//                        r += (color >> 16) & 0xff;
//                        g += (color >> 8) & 0xff;
//                        b += (color & 0xff);
//                    }
//                    hits++;
//                }
//
//                if (y >= 0) {
//                    newColors[y] = Color.argb(0xFF, (int) (r / hits), (int) (g / hits), (int) (b / hits));
//                }
//
//                index += w;
//            }
//
//            for (int y = 0; y < h; y++) {
//                pixels[y * w + x] = newColors[y];
//            }
//        }
//    }

    public static int clamp(int i, int minValue, int maxValue) {
        if (i < minValue) {
            return minValue;
        } else if (i > maxValue) {
            return maxValue;
        } else {
            return i;
        }
    }


}