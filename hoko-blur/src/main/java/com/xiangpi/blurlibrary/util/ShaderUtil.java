package com.xiangpi.blurlibrary.util;

/**
 * Created by xiangpi on 16/9/4.
 */
public class ShaderUtil {
    private static final String GAUSSIAN_SIGMA = "3.0f";

    /**
     * 预先设置Kernel权重数组，出现GPU寄存器不足，无法计算，这里在代码中直接计算kernel
     */
    public static String getGaussianSampleCode(int radius) {
        int d = radius * 2 + 1;

        StringBuilder sb = new StringBuilder();

        sb.append("  vec3 sampleTex[KERNEL_SIZE];\n")
                .append("  vec3 col;  \n")
                .append("  float weightSum = 0.0f; \n")
                .append("  for(int i = 0; i < KERNEL_SIZE; i++) {\n")
                .append("   vec2 offset = vec2(float(i - ")
                .append(radius).append(") * uWidthOffset, float(i - ")
                .append(radius).append(") * uHeightOffset);\n")
                .append("        sampleTex[i] = vec3(texture2D(uTexture, (vTexCoord.st + offset)));\n")
                .append("  } \n")
                .append("  for(int i = 0; i < KERNEL_SIZE; i++) { \n")
                .append("       float index = float(i); \n")
                .append("       float gaussWeight = getGaussWeight(index - float(KERNEL_SIZE - 1)/2.0,")
                .append(GAUSSIAN_SIGMA + "); \n")
                .append("       col += sampleTex[i] * gaussWeight; \n")
                .append("       weightSum += gaussWeight;\n")
                .append("  }   \n")
                .append("  gl_FragColor = vec4(col / weightSum, 1.0);   \n");

        return sb.toString().replace("KERNEL_SIZE", d + "");
    }

    /**
     * 预先设置Kernel权重数组，出现GPU寄存器不足，无法计算，这里在代码中直接计算kernel
     */
    public static String getBoxSampleCode(int radius) {
        StringBuilder sb = new StringBuilder();

        int d = radius * 2 + 1;

        sb.append("  vec3 sampleTex[KERNEL_SIZE];\n")
                .append("  vec3 col;  \n")
                .append("  float weightSum = 0.0f; \n")
                .append("  for(int i = 0; i < KERNEL_SIZE; i++) {\n")
                .append("   vec2 offset = vec2(float(i - ")
                .append(radius).append(") * uWidthOffset, float(i - ")
                .append(radius).append(") * uHeightOffset);\n")
                .append("        sampleTex[i] = vec3(texture2D(uTexture, (vTexCoord.st + offset)));\n")
                .append("  } \n")
                .append("  for(int i = 0; i < KERNEL_SIZE; i++) { \n")
                .append("       float index = float(i); \n")
                .append("       float boxWeight = 1.0f / float(KERNEL_SIZE); \n")
                .append("       col += sampleTex[i] * boxWeight; \n")
                .append("       weightSum += boxWeight;\n")
                .append("  }   \n")
                .append("  gl_FragColor = vec4(col / weightSum, 1.0);   \n");

        return sb.toString().replace("KERNEL_SIZE", d + "");
    }

    /**
     * 预先设置Kernel权重数组，出现GPU寄存器不足，无法计算，这里在代码中直接计算kernel
     */
    public static String getStackSampleCode(int radius) {
        StringBuilder sb = new StringBuilder();

        int d = radius * 2 + 1;

        sb.append("  vec3 sampleTex[KERNEL_SIZE];\n")
                .append("  vec3 col;  \n")
                .append("  float weightSum = 0.0f; \n")
                .append("  for(int i = 0; i < KERNEL_SIZE; i++) {\n")
                .append("   vec2 offset = vec2(float(i - ")
                .append(radius).append(") * uWidthOffset, float(i - ")
                .append(radius).append(") * uHeightOffset);\n")
                .append("        sampleTex[i] = vec3(texture2D(uTexture, (vTexCoord.st + offset)));\n")
                .append("  } \n")
                .append("  for(int i = 0; i < KERNEL_SIZE; i++) { \n")
                .append("       float index = float(i); \n")
                .append("       float boxWeight = float(")
                .append(radius)
                .append(") + 1.0f - abs(index - float(")
                .append(radius)
                .append(")); \n")
                .append("       col += sampleTex[i] * boxWeight; \n")
                .append("       weightSum += boxWeight;\n")
                .append("  }   \n")
                .append("  gl_FragColor = vec4(col / weightSum, 1.0);   \n");

        return sb.toString().replace("KERNEL_SIZE", d + "");
    }

    // 获得初始化模糊核部分的代码
    public static String getKernelInitCode(float[] kernel) {
        if (kernel == null || kernel.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder("  float kernel[" + kernel.length + "]; \n");

        for (int i = 0; i < kernel.length; i++) {
            sb.append("  kernel[");
            sb.append(i);
            sb.append("] = ");
            sb.append(kernel[i] + "f; \n");
        }

        return sb.toString();
    }

    //设置不同步长
    public static String getOffsetInitCode(int radius) {
        final int d = 2 * radius + 1;
        StringBuilder sb = new StringBuilder("  vec2 offsets[" + d + "]; \n");

        for (int i = -radius; i <= radius; i++) {
                sb.append("  offsets[")
                    .append(i + radius)
                    .append("] = vec2(")
                    .append(i)
                    .append(".f * uWidthOffset, ")
                    .append(i)
                    .append(".f * uHeightOffset); \n");
        }

        return sb.toString();

    }

    //提前设置权重值
//    public static String getSampleCode(int d) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("  vec3 sampleTex[KERNEL_SIZE];\n")
//                .append("  for(int i = 0; i < KERNEL_SIZE; i++) {\n")
//                .append("        sampleTex[i] = vec3(texture2D(uTexture, 1.0f - (vTexCoord.st + offsets[i])));\n")
//                .append("  } \n")
//                .append("  vec3 col;  \n")
//                .append("  for(int i = 0; i < KERNEL_SIZE; i++) \n")
//                .append("        col += sampleTex[i] * kernel[i]; \n")
//                .append("  gl_FragColor = vec4(col, 1.0);   \n");
//
//        return sb.toString().replace("KERNEL_SIZE", d + "");
//    }



//     //二维的
//    public static String getOffsetInitCode(int radius) {
//        final int d = 2 * radius + 1;
//        final int size = d * d;
//        StringBuilder sb = new StringBuilder("  vec2 offsets[" + size + "]; \n");
//
//        for (int i = -radius; i <= radius; i++) {
//            for (int j = radius; j >= -radius; j--) {
//                sb.append("  offsets[")
//                    .append(d * (radius - j) + i + radius)
//                    .append("] = vec2(")
//                    .append(i)
//                    .append(".f * uWidthOffset, ")
//                    .append(j)
//                    .append(".f * uHeightOffset); \n");
//            }
//        }
//
//        return sb.toString();
//
//    }
//     //二维的
//    public static String getSampleCode(int size) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("  vec3 sampleTex[KERNEL_SIZE];\n")
//            .append("  for(int i = 0; i < KERNEL_SIZE; i++) {\n")
//            .append("        sampleTex[i] = vec3(texture2D(uTexture, 1.0f - (vTexCoord.st + offsets[i])));\n")
//            .append("  } \n")
//            .append("  vec3 col;  \n")
//            .append("  for(int i = 0; i < KERNEL_SIZE; i++) \n")
//            .append("        col += sampleTex[i] * kernel[i]; \n")
//            .append("  gl_FragColor = vec4(col, 1.0);   \n");
//
//        return sb.toString().replace("KERNEL_SIZE", size + "");
//    }


}
