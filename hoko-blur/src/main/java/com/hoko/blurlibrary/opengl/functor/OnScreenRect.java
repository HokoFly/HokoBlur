package com.hoko.blurlibrary.opengl.functor;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.util.ShaderUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by xiangpi on 16/11/23.
 */
public class OnScreenRect {

    private static final String TAG = "OnScreenRect";

    private float[] mModelMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mScreenMVPMatrix = new float[16];

    private int mTargetFboId;


    private int mWidth = 200;
    private int mHeight = 200;

//    private final String vertexShaderCode =
//            "uniform mat4 uMVPMatrix;   \n" +
////                    "attribute vec2 aTexCoord;   \n" +
//                    "attribute vec4 aPosition;  \n" +
////                    "varying vec2 vTexCoord;  \n" +
//                    "void main() {              \n" +
//            "  gl_Position = uMVPMatrix * aPosition; \n" +
////                    "  gl_Position = aPosition; \n" +
////                    "  vTexCoord = aTexCoord; \n" +
//                    "}  \n";

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;   \n" +
                    "attribute vec2 aTexCoord;   \n" +
                    "attribute vec4 aPosition;  \n" +
                    "varying vec2 vTexCoord;  \n" +
                    "void main() {              \n" +
                    "  gl_Position = uMVPMatrix * aPosition; \n" +
//                    "  gl_Position = aPosition; \n" +
                    "  vTexCoord = aTexCoord; \n" +
                    "}  \n";


    private String fragmentShaderCode =
            "precision mediump float;   \n" +
                    "uniform vec4 vColor;   \n" +
//                    "varying vec2 vTexCoord;   \n" +
//                    "uniform sampler2D uTexture;   \n" +

                    "void main() {   \n" +
                    "  gl_FragColor = vColor;   \n" +
                    "}   \n";


    private FloatBuffer mVertexBuffer;

    private ShortBuffer mDrawListBuffer;

    private FloatBuffer mTexCoordBuffer;

    private static final int COORDS_PER_VERTEX = 3;

    private float squareCoords[] = {
            0.0F, 1.0F, 0.0F, // top left
            0.0F, 0.0F, 0.0F, // bottom left
            1.0F, 0.0F, 0.0F, // bottom right
            1.0F, 1.0F, 0.0F}; // top right

    private static float mTexHorizontalCoords[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f};

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private float fragmentColor[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

    private int mVertexShader;
    private int mFragmentShader;

    private int mProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTexCoordHandle;

    private int mWidthOffsetHandle;
    private int mHeightOffsetHandle;

    private int vertexStride = COORDS_PER_VERTEX * 4;

    private int mHorizontalTextureId;
    //    private int mVerticalTextureId;
    private int mTextureId;
    private int mTextureUniformHandle;

    private int mHorizontalFrameBuffer;
//    private int mVerticalFrameBuffer;

    private boolean inited = false;

    public OnScreenRect() {
        fragmentShaderCode = getFragmentShaderCode(6, Blur.MODE_GAUSSIAN);

        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(squareCoords);
        mVertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(drawOrder);
        mDrawListBuffer.position(0);

        ByteBuffer tcb = ByteBuffer.allocateDirect(mTexHorizontalCoords.length * 4);
        tcb.order(ByteOrder.nativeOrder());
        mTexCoordBuffer = tcb.asFloatBuffer();
        mTexCoordBuffer.put(mTexHorizontalCoords);
        mTexCoordBuffer.position(0);
//
//        Matrix.setIdentityM(mVMatrix, 0);
////
////        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);
//
//            Matrix.frustumM(mProjMatrix, 0, -1, 1, -1, 1, 3, 7);
//        // TODO: 16/12/19 调整尺寸和比例
//        Matrix.scaleM(mProjMatrix, 0, 0.6f, 0.55f, 1f);
////        Matrix.translateM(mProjMatrix, 0, -0.2f, -0.327f, 0);
//
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
//


    }

    private void initProgram() {
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, mVertexShader);
        GLES20.glAttachShader(mProgram, mFragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void handleGlInfo(DrawFunctor.GLInfo info) {

        mWidth = info.clipRight - info.clipLeft;
        mHeight = info.clipBottom - info.clipTop;

        if (mWidth <= 0 || mHeight <= 0) {
            return;
        }

        if (!inited) {
            EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
            if (context.equals(EGL10.EGL_NO_CONTEXT)) {
                Log.e(TAG, "This thread is no EGLContext.");
                return;
            }

            /**
             *
             *  Model                View           Projection
             * transform            Identity        Width Height
             * scaled Width Height  Identity        viewport Width Height
             */


//            Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);
            Matrix.setIdentityM(mVMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.scaleM(mModelMatrix, 0, mWidth, mHeight, 1.0f);
//            Matrix.scaleM(mModelMatrix, 0, 0.4f, 0.4f, 1.0f);

            Matrix.setIdentityM(mProjMatrix, 0);
            Matrix.orthoM(mProjMatrix, 0, 0, mWidth, 0, mHeight, -100f, 100f);
//            Matrix.frustumM(mProjMatrix, 0, -1, 1, -1, 1, 3, 7);
            // TODO: 16/12/19 调整尺寸和比例
//        Matrix.scaleM(mProjMatrix, 0, 0.6f, 0.55f, 1f);
//        Matrix.translateM(mProjMatrix, 0, -0.2f, -0.327f, 0);

            Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);


            System.arraycopy(info.transform, 0, mModelMatrix, 0, 16);
            Matrix.scaleM(mModelMatrix, 0, mWidth, mHeight, 1f);
            Matrix.setIdentityM(mProjMatrix, 0);
            Matrix.orthoM(mProjMatrix, 0, 0, info.viewportWidth, info.viewportHeight, 0, -100f, 100f);

            Matrix.multiplyMM(mScreenMVPMatrix, 0, mVMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mScreenMVPMatrix, 0, mProjMatrix, 0, mScreenMVPMatrix, 0);

            GLES20.glViewport(0, 0, mWidth, mHeight);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);

            mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            initProgram();

            // 获得当前绑定的FBO（屏上）
            int[] displayFbo = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, displayFbo, 0);
            mTargetFboId = displayFbo[0];

            inited = true;

        }

        for (int i = 0; i < mScreenMVPMatrix.length; i++) {
            Log.e(TAG, "handleGlInfo: mvpmatrix[" + i + "] = " + mScreenMVPMatrix[i]);
        }

        GLES20.glUseProgram(mProgram);

        GLES20.glViewport(0, 0, mWidth, mHeight);

        // TODO: 16/12/7 尺寸
        mTextureId = loadTexture(mWidth, mHeight);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, info.clipLeft, info.viewportHeight - info.clipBottom, mWidth, mHeight);

        mHorizontalTextureId = loadTexture(mWidth, mHeight);
        mHorizontalFrameBuffer = genFrameBuffer(mHorizontalTextureId);

        drawHorizontalBlur(mMVPMatrix);
        resetAllBuffer();
        GLES20.glViewport(0, 0, info.viewportWidth, info.viewportHeight);

        drawVerticalBlur(mScreenMVPMatrix);
        resetAllBuffer();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mTargetFboId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        GLES20.glDeleteTextures(2, new int[]{mTextureId, mHorizontalTextureId}, 0);
        GLES20.glDeleteFramebuffers(1, new int[]{mHorizontalFrameBuffer}, 0);


    }

    private void initTexParameter(int textureId) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }

    private int loadTexture(int width, int height) {
        final int[] textureId = new int[1];

        GLES20.glGenTextures(1, textureId, 0);

        if (textureId[0] != 0) {
            initTexParameter(textureId[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer) null);

        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return textureId[0];

    }


    private int genFrameBuffer(int texture) {
        final int[] frameBufferIds = new int[1];

        GLES20.glGenFramebuffers(1, frameBufferIds, 0);

        if (frameBufferIds[0] != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferIds[0]);
        }

        if (texture != 0) {
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, texture, 0);
        }

//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return frameBufferIds[0];

    }


    private void drawHorizontalBlur(float[] mvpMatrix) {
//        GLES20.glUseProgram(mProgram);
//
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mHorizontalFrameBuffer);

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mWidthOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uWidthOffset");
        mHeightOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetHandle, 0f / mWidth);
        GLES20.glUniform1f(mHeightOffsetHandle, 1f / mHeight);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        GLES20.glUseProgram(0);

    }

    private void drawVerticalBlur(float[] mvpMatrix) {
//        GLES20.glUseProgram(mProgram);

//        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

//        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

//        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
//        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mTargetFboId);

//        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHorizontalTextureId);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

//        mWidthOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uWidthOffset");
//        mHeightOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetHandle, 1f / mWidth);
        GLES20.glUniform1f(mHeightOffsetHandle, 0f / mHeight);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        GLES20.glUseProgram(0);

    }

    private void resetAllBuffer() {
        mVertexBuffer.rewind();
        mTexCoordBuffer.rewind();
        mDrawListBuffer.rewind();
    }

    private String getFragmentShaderCode(int radius, @Blur.BlurMode int mode) {

        StringBuilder sb = new StringBuilder();
        sb.append(" \n")
                .append("precision mediump float;")
//                .append("uniform vec4 vColor;   \n")
                .append("varying vec2 vTexCoord;   \n")
                .append("uniform sampler2D uTexture;   \n")
                .append("uniform float uWidthOffset;  \n")
                .append("uniform float uHeightOffset;  \n")
                .append("mediump float getGaussWeight(mediump float currentPos, mediump float sigma) \n")
                .append("{ \n")
                .append("   return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma)); \n")
                .append("} \n")
                .append("void main() {   \n");

        if (mode == Blur.MODE_BOX) {
            sb.append(ShaderUtil.getBoxSampleCode(radius));
        } else if (mode == Blur.MODE_GAUSSIAN) {
            sb.append(ShaderUtil.getGaussianSampleCode(radius));
        } else if (mode == Blur.MODE_STACK) {
            sb.append(ShaderUtil.getStackSampleCode(radius));
        }
        sb.append("}   \n");

        return sb.toString();
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


}
