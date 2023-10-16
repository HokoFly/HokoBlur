#pragma version(1)

#pragma rs java_package_name(com.hoko.blur.renderscript)

#pragma rs_fp_relaxed

rs_allocation input;
rs_allocation output;

int width;
int height;

int radius;


void __attribute__((kernel)) stackblur_h(uchar4 in, int32_t x, int32_t y) {

    float4 sum = 0;
    uchar4 result;
    int count = 0;
    int kernel = (2 * radius + 1);
    int weightSum = 0;

    uchar4 center = rsGetElementAt_uchar4(input, x, y);

    for (int i = -radius; i <= radius; i++) {
        if (x + i >= 0 && x + i < width) {
            uchar4 temp = rsGetElementAt_uchar4(input, x + i, y);
            int weight = radius + 1 - abs(i);
            sum += rsUnpackColor8888(temp) * weight;
            weightSum += weight;
        }

    }

    sum = sum / weightSum;
    result = rsPackColorTo8888(sum);
    result.a = center.a;
    rsSetElementAt_uchar4(output, result, x, y);

}

void __attribute__((kernel)) stackblur_v(uchar4 in, int32_t x, int32_t y) {

    float4 sum = 0;
    uchar4 result;
    int count = 0;
    int kernel = (2 * radius + 1);
    int weightSum = 0;

    uchar4 center = rsGetElementAt_uchar4(input, x, y);

    for (int j = -radius; j <= radius; j++) {
        if (y + j >= 0 && y + j < height) {
            uchar4 temp = rsGetElementAt_uchar4(input, x, y + j);
            int weight = radius + 1 - abs(j);
            sum += rsUnpackColor8888(temp) * weight;
            weightSum += weight;
        }
    }

    sum = sum / weightSum;
    result = rsPackColorTo8888(sum);
    result.a = center.a;
    rsSetElementAt_uchar4(output, result, x, y);

}


