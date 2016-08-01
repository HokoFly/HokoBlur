#pragma version(1)

#pragma rs java_package_name(com.example.xiangpi.dynamicblurdemo.activity)

#pragma rs_fp_relaxed

rs_allocation input;
rs_allocation output;
int radius;

void __attribute__((kernel)) boxblur(uchar4 in, uint32_t x, uint32_t y) {

    float4 sum = 0;
    uchar4 result;
    int count = 0;
    int kernel = (2 * radius + 1);

    for (int i = -radius; i <= radius; i++) {
        for (int j = -radius; j <= radius; j++) {
            uchar4 temp = rsGetElementAt_uchar4(input, x + i, y + j);
            sum += rsUnpackColor8888(temp);
            count++;
        }
    }

    sum = sum / count;
    result = rsPackColorTo8888(sum);
    result.a = 255;
    rsSetElementAt_uchar4(output, result, x, y);
}

