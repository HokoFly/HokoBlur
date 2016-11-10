package com.hoko.blurlibrary.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;


import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by xiangpi on 16/8/10.
 */
public class OffScreenRectangle {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;   \n" +
            "attribute vec2 aTexCoord;   \n" +
            "attribute vec4 aPosition;  \n" +
            "varying vec2 vTexCoord;  \n" +
            "void main() {              \n" +
//            "  gl_Position = uMVPMatrix * aPosition; \n" +
            "  gl_Position = aPosition; \n" +
            "  vTexCoord = aTexCoord; \n" +
            "}  \n";

    private final String fragmentShaderCode;


    private FloatBuffer mVertexBuffer;

    private ShortBuffer mDrawListBuffer;

    private FloatBuffer mTexCoordBuffer;

    private static final int COORDS_PER_VERTEX = 3;

    private float squareCoords[] = {
                -1f,  1f, 0.0f,   // top left
                -1f, -1f, 0.0f,   // bottom left
                1f, -1f, 0.0f,   // bottom right
                1f,  1f, 0.0f }; // top right

    private static float mTexHorizontalCoords[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f};
//    private static float mTexVerticalCoords[] = {
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            0.0f, 0.0f,
//            0.0f, 1.0f};

//    private static float mTexVerticalCoords[] = {
//            1.0f, 0.0f,
//            1.0f, 1.0f,
//            0.0f, 1.0f,
//            0.0f, 0.0f};

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private float fragmentColor[] = {00.2f, 0.709803922f, 0.898039216f, 1.0f };

    private int mVertexShader;
    private int mFragmentShader;

    private int mHorizontalProgram;
    private int mVerticalProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTexCoordHandle;

    private int mWidthOffsetHandle;
    private int mHeightOffsetHandle;

    private int vertexStride = COORDS_PER_VERTEX * 4;

    private Bitmap mBitmap;

    private int mHorizontalTexture;
    private int mVerticalTexture;
    private int mInputTexture;
    private int mTextureUniformHandle;

    private int mHorizontalFrameBuffer;

    private int mWidth;
    private int mHeight;

    public OffScreenRectangle(int blurRadius, @Blur.BlurMode int mode) {

        fragmentShaderCode = getFragmentShaderCode(blurRadius, mode);

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


    }

    public void setInputBitmap(Bitmap bitmap) {

        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);

        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mBitmap = bitmap;

        if (mBitmap != null) {
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
        }
        mInputTexture = loadTexture(mBitmap);

        initHorizontal();
        initVertical();

    }

    private void initHorizontal() {
        mHorizontalProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mHorizontalProgram, mVertexShader);
        GLES20.glAttachShader(mHorizontalProgram, mFragmentShader);
        GLES20.glLinkProgram(mHorizontalProgram);
        mHorizontalTexture = loadTexture(mWidth, mHeight);
        mHorizontalFrameBuffer = genFrameBuffer(mHorizontalTexture);

    }

    private void initVertical() {
        mVerticalProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mVerticalProgram, mVertexShader);
        GLES20.glAttachShader(mVerticalProgram, mFragmentShader);
        GLES20.glLinkProgram(mVerticalProgram);
        mVerticalTexture = loadTexture(mWidth, mHeight);

    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
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

    private int loadTexture(Bitmap bitmap) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureHandle[0];

    }

    private int genFrameBuffer(int texture) {
        final int[] frameBufferHandle = new int[1];

        GLES20.glGenFramebuffers(1, frameBufferHandle, 0);

        if (frameBufferHandle[0] != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferHandle[0]);
        }

        if (texture != 0) {
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, texture, 0);
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return  frameBufferHandle[0];
    }

    private int loadTexture(int width, int height) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureHandle[0];

    }

    public void draw(float[] mvpMatrix) {
        drawHorizontalBlur(mvpMatrix);
        resetAllBuffer();
        drawVerticalBlur(mvpMatrix);
        resetAllBuffer();

    }

    private void drawHorizontalBlur(float[] mvpMatrix) {
        GLES20.glUseProgram(mHorizontalProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mHorizontalProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

//        mColorHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        mTexCoordHandle = GLES20.glGetAttribLocation(mHorizontalProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mHorizontalFrameBuffer);

        mTextureUniformHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputTexture);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mWidthOffsetHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uWidthOffset");
        mHeightOffsetHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetHandle, 0f / mWidth);
        GLES20.glUniform1f(mHeightOffsetHandle, 1f / mHeight);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

    }

    private void drawVerticalBlur(float[] mvpMatrix) {
        GLES20.glUseProgram(mVerticalProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mVerticalProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mVerticalProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

//        mTexCoordBuffer.clear();
//        mTexCoordBuffer.put(mTexVerticalCoords);
//        mTexCoordBuffer.position(0);
        mTexCoordHandle = GLES20.glGetAttribLocation(mVerticalProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        mTextureUniformHandle = GLES20.glGetUniformLocation(mVerticalProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHorizontalTexture);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mWidthOffsetHandle = GLES20.glGetUniformLocation(mVerticalProgram, "uWidthOffset");
        mHeightOffsetHandle = GLES20.glGetUniformLocation(mVerticalProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetHandle, 1f / mWidth);
        GLES20.glUniform1f(mHeightOffsetHandle, 0f / mHeight);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

    }

    private void resetAllBuffer() {
        mVertexBuffer.rewind();
        mTexCoordBuffer.rewind();
        mDrawListBuffer.rewind();
    }
}
