package com.hoko.blur.util;

import android.opengl.GLES20;
import android.util.Log;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.anno.Mode;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by yuxfzju on 16/9/4.
 */
public class ShaderUtil {

    private static final String TAG = ShaderUtil.class.getSimpleName();

    public static int createProgram(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        if (vertexShader == 0 || fragmentShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragmentShader);
            checkGLError("Attach Shader");
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != 1)  {
                Log.e(TAG, "Failed to link program");
                GLES20.glDeleteProgram(program);
                program = 0;
            }

        }
        return program;
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        if (shader != 0) {
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            final int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Failed to compile the shader");
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static String getVertexCode() {
        StringBuilder sb = new StringBuilder();

        sb.append("uniform mat4 uMVPMatrix;   \n")
                .append("uniform mat4 uTexMatrix;   \n")
                .append("attribute vec2 aTexCoord;   \n")
                .append("attribute vec3 aPosition;  \n")
                .append("varying vec2 vTexCoord;  \n")
                .append("void main() {              \n")
                .append("  gl_Position = uMVPMatrix * vec4(aPosition, 1); \n")
                .append("     vTexCoord = (uTexMatrix * vec4(aTexCoord,0,1)).st;\n")
                .append("}  \n");

        return sb.toString();

    }

    /**
     * 返回true为GL无Error
     * @param msg
     * @return
     */
    public static boolean checkGLError(String msg) {
        int error = GLES20.glGetError();
        if (error != 0) {
            Log.e(TAG, "checkGLError: " + msg);
        }

        return error == 0;
    }

    public static boolean checkEGLContext() {
        EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        if (context.equals(EGL10.EGL_NO_CONTEXT)) {
            Log.e(TAG, "This thread is no EGLContext.");
            return false;
        } else {
            return true;
        }
    }

    public static String getFragmentShaderCode(@Mode int mode) {

        StringBuilder sb = new StringBuilder();
        sb.append(" \n")
                .append("precision mediump float;   \n")
                .append("varying vec2 vTexCoord;   \n")
                .append("uniform sampler2D uTexture;   \n")
                .append("uniform int uRadius;   \n")
                .append("uniform float uWidthOffset;  \n")
                .append("uniform float uHeightOffset;  \n")
                .append("mediump float getGaussWeight(mediump float currentPos, mediump float sigma) \n")
                .append("{ \n")
                .append("   return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma)); \n")
                .append("} \n")

                /**
                 * Android 4.4一下系统编译器优化，这里注释暂时不用的GLSL代码
                 */
                .append("void main() {   \n");

        if (mode == HokoBlur.MODE_BOX) {
            sb.append(ShaderUtil.getBoxSampleCode());
        } else if (mode == HokoBlur.MODE_GAUSSIAN) {
            sb.append(ShaderUtil.getGaussianSampleCode());
        } else if (mode == HokoBlur.MODE_STACK) {
            sb.append(ShaderUtil.getStackSampleCode());
        }
        sb.append("}   \n");

        return sb.toString();
    }



    /**
     * 预先设置Kernel权重数组，出现GPU寄存器不足，无法计算，这里在代码中直接计算kernel
     */
    private static String getGaussianSampleCode() {

        StringBuilder sb = new StringBuilder();

        sb.append("int diameter = 2 * uRadius + 1;  \n")
                .append("  vec3 sampleTex;\n")
                .append("  vec3 col;  \n")
                .append("  float weightSum = 0.0; \n")
                .append("  for(int i = 0; i < diameter; i++) {\n")
                .append("       vec2 offset = vec2(float(i - uRadius) * uWidthOffset, float(i - uRadius) * uHeightOffset);  \n")
                .append("       sampleTex = vec3(texture2D(uTexture, vTexCoord.st+offset));\n")
                .append("       float index = float(i); \n")
                .append("       float gaussWeight = getGaussWeight(index - float(diameter - 1)/2.0,")
                .append("           (float(diameter - 1)/2.0 + 1.0) / 2.0); \n")
                .append("       col += sampleTex * gaussWeight; \n")
                .append("       weightSum += gaussWeight;\n")
                .append("  }   \n")
                .append("  gl_FragColor = vec4(col / weightSum, 1.0);   \n");

        return sb.toString();
    }

    /**
     * 预先设置Kernel权重数组，出现GPU寄存器不足，无法计算，这里在代码中直接计算kernel
     */
    private static String getBoxSampleCode() {
        StringBuilder sb = new StringBuilder();

        sb.append("int diameter = 2 * uRadius + 1; \n")
                .append("  vec3 sampleTex;\n")
                .append("  vec3 col;  \n")
                .append("  float weightSum = 0.0; \n")
                .append("  for(int i = 0; i < diameter; i++) {\n")
                .append("       vec2 offset = vec2(float(i - uRadius) * uWidthOffset, float(i - uRadius) * uHeightOffset);  \n")
                .append("        sampleTex = vec3(texture2D(uTexture, vTexCoord.st+offset));\n")
                .append("       float index = float(i); \n")
                .append("       float boxWeight = float(1.0) / float(diameter); \n")
                .append("       col += sampleTex * boxWeight; \n")
                .append("       weightSum += boxWeight;\n")
                .append("  }   \n")
                .append("  gl_FragColor = vec4(col / weightSum, 1.0);   \n");
        return sb.toString();
    }

    /**
     * 预先设置Kernel权重数组，出现GPU寄存器不足，无法计算，这里在代码中直接计算kernel
     */
    private static String getStackSampleCode() {
        StringBuilder sb = new StringBuilder();

        sb.append("int diameter = 2 * uRadius + 1;  \n")
                .append("  vec3 sampleTex;\n")
                .append("  vec3 col;  \n")
                .append("  float weightSum = 0.0; \n")
                .append("  for(int i = 0; i < diameter; i++) {\n")
                .append("       vec2 offset = vec2(float(i - uRadius) * uWidthOffset, float(i - uRadius) * uHeightOffset);  \n")
                .append("       sampleTex = vec3(texture2D(uTexture, vTexCoord.st+offset));\n")
                .append("       float index = float(i); \n")
                .append("       float boxWeight = float(uRadius) + 1.0 - abs(index - float(uRadius)); \n")
                .append("       col += sampleTex * boxWeight; \n")
                .append("       weightSum += boxWeight;\n")
                .append("  }   \n")
                .append("  gl_FragColor = vec4(col / weightSum, 1.0);   \n");

        return sb.toString();
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

    /**
     * 获得与输入纹理相同的纹理
     * @return
     */
    public static String getCopyFragmentCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(" \n")
                .append("precision mediump float;")
                .append("varying vec2 vTexCoord;   \n")
                .append("uniform sampler2D uTexture;   \n")
                .append("void main() {   \n")
                .append("  vec3 col = vec3(texture2D(uTexture, vTexCoord.st));\n")
                .append("  gl_FragColor = vec4(col, 1.0);   \n")
                .append("}   \n");
        return sb.toString();
    }


    /**
     * 获得纯色的Fragment
     * @return
     */
    public static String getColorFragmentCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("precision mediump float;   \n")
                .append("uniform vec4 vColor;   \n")
                .append("void main() {   \n")
                .append("   gl_FragColor = vColor;   \n")
                .append("} \n");

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



}
