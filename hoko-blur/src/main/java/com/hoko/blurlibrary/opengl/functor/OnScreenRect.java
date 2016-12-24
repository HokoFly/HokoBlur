package com.hoko.blurlibrary.opengl.functor;

import android.graphics.Rect;
import android.graphics.RectF;
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

import static android.opengl.GLES20.GL_CULL_FACE;

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

    private float[] mTexMatrix = new float[16];
    private int mTargetFboId;


    private int mWidth = 200;
    private int mHeight = 200;

    private float minFactor = 1f;
    private float factor = 1f;

    private RectF bound1;
    private RectF bound2;

    private int mViewportW;
    private int mViewportH;

    private int mW;
    private int mH;

    private int mFboW;
    private int mFboH;

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
            "uniform mat4 uTexMatrix;   \n" +
//                    "uniform int uVertical; \n" +
                    "attribute vec2 aTexCoord;   \n" +
                    "attribute vec3 aPosition;  \n" +
                    "varying vec2 vTexCoord;  \n" +
                    "void main() {              \n" +
                    "  gl_Position = uMVPMatrix * vec4(aPosition, 1); \n" +
//                    "  gl_Position = aPosition; \n" +
                    "     vTexCoord = (uTexMatrix * vec4(aTexCoord,0,1)).st;\n" +
//                    "   vTexCoord = aTexCoord;\n" +
//                    "if (uVertical == 0) {\n" +
//                    "   vTexCoord.y = float(1) - vTexCoord.y;\n" +
//                    "}\n" +
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
            0.0F, 0.0F, 0.0F, // top left
            1.0F, 0.0F, 0.0F, // bottom left
            0.0F, 1.0F, 0.0F, // bottom right
            1.0F, 1.0F, 0.0F}; // top right

    private static float mTexHorizontalCoords[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f};

    private short drawOrder[] = { 0, 1, 2, 2, 3,1 };
//    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private float fragmentColor[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

    private int mVertexShader;
    private int mFragmentShader;
    private int mCopyFragmentShader;


    private int mProgram;
    private int mCopyProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTexCoordHandle;
    private int mTexMatrixHandle;
    private int mVerticalHandle;
    private int mBoundsHandle;
    private int mRadiusHandle;
    private int mWeightHandle;
    private int mStepHandle;

    private int mWidthOffsetHandle;
    private int mHeightOffsetHandle;

    private int vertexStride = COORDS_PER_VERTEX * 4;

    private int mHorizontalTextureId;
        private int mVerticalTextureId;
    private int mTextureId;
    private int mTextureUniformHandle;

    private int mHorizontalFrameBuffer;
    private int mVerticalFrameBuffer;

    private boolean inited = false;
    private DrawFunctor.GLInfo mInfo;
    private Rect mClipBounds = new Rect();
    private Rect mSourceBounds = new Rect();
    private Rect mTargetBounds = new Rect();

    public OnScreenRect() {
        fragmentShaderCode = getFragmentShaderCode(4, Blur.MODE_GAUSSIAN);
//        fragmentShaderCode = getFragmentShaderCode();

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

    private void initProgram() {
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, mVertexShader);
        GLES20.glAttachShader(mProgram, mFragmentShader);
        GLES20.glLinkProgram(mProgram);

        mCopyProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mCopyProgram, mVertexShader);
        GLES20.glAttachShader(mCopyProgram, mCopyFragmentShader);
        GLES20.glLinkProgram(mCopyProgram);
    }

    public void handleGlInfo(DrawFunctor.GLInfo info) {
        mInfo = info;

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

            initSize();

            /**
             *
             *  Model                View           Projection
             * transform            Identity        Width Height
             * scaled Width Height  Identity        viewport Width Height
             */


//            Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);
            Matrix.setIdentityM(mMVPMatrix, 0);
            Matrix.setIdentityM(mVMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.scaleM(mModelMatrix, 0, mWidth, mHeight, 1.0f);
//            Matrix.scaleM(mModelMatrix, 0, 0.4f, 0.4f, 1.0f);

            Matrix.setIdentityM(mProjMatrix, 0);
            Matrix.orthoM(mProjMatrix, 0, 0, info.viewportWidth,  0, info.viewportHeight, -100f, 100f);
//            Matrix.frustumM(mProjMatrix, 0, -1, 1, -1, 1, 3, 7);
            // TODO: 16/12/19 调整尺寸和比例
//        Matrix.scaleM(mProjMatrix, 0, 0.6f, 0.55f, 1f);
//        Matrix.translateM(mProjMatrix, 0, -0.2f, -0.327f, 0);

            Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

            Matrix.setIdentityM(mScreenMVPMatrix, 0);
//            Matrix.setIdentityM(mModelMatrix, 0);

            System.arraycopy(info.transform, 0, mModelMatrix, 0, 16);
            Matrix.scaleM(mModelMatrix, 0, mWidth, mHeight, 1f);
            Matrix.setIdentityM(mProjMatrix, 0);
            Matrix.orthoM(mProjMatrix, 0, 0, info.viewportWidth, info.viewportHeight, 0, -100f, 100f);

            Matrix.multiplyMM(mScreenMVPMatrix, 0, mVMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mScreenMVPMatrix, 0, mProjMatrix, 0, mScreenMVPMatrix, 0);


            mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);


            mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getCopyFragmentCode());
            mCopyFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


//            initProgram();



            inited = true;

        }

        // 获得当前绑定的FBO（屏上）
        int[] displayFbo = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, displayFbo, 0);
        mTargetFboId = displayFbo[0];
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        GLES20.glBlendFunc(770, 771);

        for (int i = 0; i < mScreenMVPMatrix.length; i++) {
            Log.e(TAG, "handleGlInfo: mvpmatrix[" + i + "] = " + mScreenMVPMatrix[i]);
        }

        initProgram();

        GLES20.glUseProgram(mProgram);

        GLES20.glViewport(0, 0, mW, mH);

        // TODO: 16/12/7 尺寸
        mTextureId = loadTexture(mWidth, mHeight);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        aaa();
//        GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, 0, 0, mWidth, mHeight);

        mHorizontalTextureId = loadTexture(mWidth, mHeight);
        mHorizontalFrameBuffer = genFrameBuffer(mHorizontalTextureId);
        mVerticalTextureId = loadTexture(mW, mH);
        mVerticalFrameBuffer = genFrameBuffer(mVerticalTextureId);

        getTexMatrix(false);
        GLES20.glViewport(0, 0, info.viewportWidth, info.viewportHeight);

        drawHorizontalBlur(mMVPMatrix, mTexMatrix);
        resetAllBuffer();


//        Matrix.setIdentityM(mScreenMVPMatrix, 0);

//        drawVerticalBluraa(mMVPMatrix, mTexMatrix);
//        resetAllBuffer();

        getTexMatrix(true);
        GLES20.glViewport(0, 0, info.viewportWidth, info.viewportHeight);


        drawVerticalBlur(mScreenMVPMatrix, mTexMatrix);
        resetAllBuffer();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mTargetFboId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        GLES20.glDeleteTextures(2, new int[]{mTextureId, mHorizontalTextureId, mVerticalTextureId}, 0);
        GLES20.glDeleteFramebuffers(1, new int[]{mHorizontalFrameBuffer, mVerticalFrameBuffer}, 0);
        GLES20.glDeleteProgram(mProgram);
        GLES20.glDeleteProgram(mCopyProgram);


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


    private void drawHorizontalBlur(float[] mvpMatrix, float[] texMatrix) {
        GLES20.glUseProgram(mProgram);
//
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        mTexMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(mTexMatrixHandle, 1, false, texMatrix, 0);

//        mVerticalHandle = GLES20.glGetUniformLocation(mProgram, "uVertical");
//        GLES20.glUniform1i(mVerticalHandle, 0);

//        mRadiusHandle = GLES20.glGetUniformLocation(mProgram, "uRadius");
//        GLES20.glUniform1i(mRadiusHandle, 5);
//
//        mWeightHandle = GLES20.glGetUniformLocation(mProgram, "uWeight");
//        GLES20.glUniform1f(mWeightHandle, 1.0f / 11);
//
//        mStepHandle = GLES20.glGetUniformLocation(mProgram, "uStep");
//        GLES20.glUniform2f(mStepHandle, 0.0f, 1f / mHeight);

//        mBoundsHandle = GLES20.glGetUniformLocation(mProgram, "uBounds");
//        GLES20.glUniform4f(mBoundsHandle, bound1.left, bound1.right, bound1.top, bound1.bottom);

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
//        GLES20.glDrawArrays(5, 0, 4);
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        GLES20.glUseProgram(0);

//            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_CULL_FACE);

//            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

    }

    private void drawVerticalBlur(float[] mvpMatrix, float[] texMatrix) {
        GLES20.glUseProgram(mCopyProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mCopyProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mCopyProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        mTexMatrixHandle = GLES20.glGetUniformLocation(mCopyProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(mTexMatrixHandle, 1, false, texMatrix, 0);

//        GLES20.glUniform1i(mVerticalHandle, 0);

//        GLES20.glUniform1i(mRadiusHandle, 11);
//
//        GLES20.glUniform1f(mWeightHandle, 1.0f / 11);
//
//        GLES20.glUniform2f(mStepHandle, 1f / mWidth, 0.0f);

//        GLES20.glUniform4f(mBoundsHandle, bound2.left, bound2.right, bound2.top, bound2.bottom);

//        mColorHandle = GLES20.glGetUniformLocation(mCopyProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

        mTexCoordHandle = GLES20.glGetAttribLocation(mCopyProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mTargetFboId);

        mTextureUniformHandle = GLES20.glGetUniformLocation(mCopyProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHorizontalTextureId);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mWidthOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uWidthOffset");
        mHeightOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetHandle, 1f / mWidth / minFactor);
        GLES20.glUniform1f(mHeightOffsetHandle, 0f / mHeight);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
//        GLES20.glDrawArrays(5, 0, 4);

//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        GLES20.glUseProgram(0);

//        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

//        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);


    }

    private void drawVerticalBluraa(float[] mvpMatrix, float[] texMatrix) {
//        GLES20.glUseProgram(mProgram);

//        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
//        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

//        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glUniformMatrix4fv(mTexMatrixHandle, 1, false, texMatrix, 0);

//        GLES20.glUniform1i(mVerticalHandle, 1);

//        GLES20.glUniform1i(mRadiusHandle, 11);
//
//        GLES20.glUniform1f(mWeightHandle, 1.0f / 11);
//
//        GLES20.glUniform2f(mStepHandle, 1f / mWidth, 0.0f);

        GLES20.glUniform4f(mBoundsHandle, bound2.left, bound2.right, bound2.top, bound2.bottom);

//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

//        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
//        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mVerticalFrameBuffer);

//        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHorizontalTextureId);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

//        mWidthOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uWidthOffset");
//        mHeightOffsetHandle = GLES20.glGetUniformLocation(mProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetHandle, 1f / mWidth / minFactor);
        GLES20.glUniform1f(mHeightOffsetHandle, 0f / mHeight);

//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDrawArrays(5, 0, 4);

//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        GLES20.glUseProgram(0);

//        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

//        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);


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
                .append("uniform vec4 uBounds;   \n")
                .append("varying vec2 vTexCoord;   \n")
                .append("uniform sampler2D uTexture;   \n")
                .append("uniform float uWidthOffset;  \n")
                .append("uniform float uHeightOffset;  \n")
                .append("mediump float getGaussWeight(mediump float currentPos, mediump float sigma) \n")
                .append("{ \n")
                .append("   return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma)); \n")
                .append("} \n")
                .append("float clip(float x,float min,float max) { \n")
                .append("    if (x>max) {\n")
                .append("       x=max;  \n")
                .append("    } else if (x<min) {\n")
                .append("       x=min;  \n")
                .append("    }\n")
                .append("    return x;  \n")
                .append("} ")
                .append("vec2 getTexCoord(vec2 texcoord,vec2 step) { \n")
                .append("return vec2(clip(texcoord.x+step.x,uBounds.x,uBounds.y), clip(texcoord.y+step.y,uBounds.z,uBounds.w));\n")
                .append("} ")
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

    private String getCopyFragmentCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(" \n")
                .append("precision mediump float;")
                .append("varying vec2 vTexCoord;   \n")
                .append("uniform sampler2D uTexture;   \n")
                .append("void main() {   \n")
                .append("  vec3 col = vec3(texture2D(uTexture, vTexCoord.st));\n")
                .append("  gl_FragColor = vec4(col, 1.0);   \n")
                .append("}   \n");
//        sb.append("precision mediump float;   \n" +
//                "uniform vec4 vColor;   \n" +
////                    "varying vec2 vTexCoord;   \n" +
////                    "uniform sampler2D uTexture;   \n" +
//
//                "void main() {   \n" +
//                "  gl_FragColor = vColor;   \n" +
//                "}   \n");
        return sb.toString();
//        return getFragmentShaderCode(6, Blur.MODE_GAUSSIAN);

    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private void initSize() {
        mViewportW = (int) (mWidth * factor);
        mViewportH = (int) (mHeight * factor);

        mW = mViewportW + 8;
        mH = mViewportH + 8;

        mFboW = nextMultipleN(mWidth * minFactor + 8.0f, 4);
        mFboH = nextMultipleN(mHeight * minFactor + 8.0f, 4);

        Log.e(TAG, "getTexMatrix: viewportW: " + mViewportW);
        Log.e(TAG, "getTexMatrix: viewportH: " + mViewportH);
        Log.e(TAG, "getTexMatrix: fboW: " + mFboW);
        Log.e(TAG, "getTexMatrix: fboH: " + mFboH);

        float right = (float)mViewportW / (float)mFboW;
        float bottom = (float)mViewportH / (float)mFboH;

        float scaleX = (float)mW / (float)(mW - 8);
        float scaleY = (float)mH / (float)(mH - 8);

        bound1 = new RectF(0, 0, scaleX * right, scaleY * bottom);
        bound2 = new RectF(0, 0, scaleX, scaleY);

        Log.e(TAG, "getTexMatrix: right: " + right);
        Log.e(TAG, "getTexMatrix: bottom: " + bottom);
        Log.e(TAG, "getTexMatrix: scaleX: " + scaleX);
        Log.e(TAG, "getTexMatrix: scaleY: " + scaleY);
    }

    private void getTexMatrix(boolean flipY){
        Matrix.setIdentityM(mTexMatrix, 0);

        if (flipY) {
            Matrix.translateM(mTexMatrix, 0, bound2.left, bound2.bottom, 0);
            Matrix.scaleM(mTexMatrix, 0, bound2.width(), -bound2.height(), 1.0F);
//            Matrix.translateM(mTexMatrix, 0, bound2.left, bound2.top, 0);
//            Matrix.scaleM(mTexMatrix, 0, bound2.width(), bound2.height(), 1.0F);

        } else {
            Matrix.translateM(mTexMatrix, 0, bound1.left, bound1.top, 0);
            Matrix.scaleM(mTexMatrix, 0, bound1.width(), bound1.height(), 1.0F);

        }

    }

    private int nextMultipleN(float value, int n) {
        return (int)(value + (float)n - 1.0F) / n * n;
    }


    private void aaa() {
        window2View(mInfo.transform, (float)mInfo.clipLeft, (float)mInfo.clipTop, (float)mInfo.clipRight, (float)mInfo.clipBottom, this.mClipBounds);
        mClipBounds.intersect(this.mSourceBounds);
        view2Window(mInfo.transform, this.mClipBounds, this.mTargetBounds);
//        GLES20.glCopyTexSubImage2D(3553, 0, Math.abs(this.mClipBounds.left - this.mSourceBounds.left), Math.abs(this.mClipBounds.bottom - this.mSourceBounds.bottom),
//                this.mTargetBounds.left, mInfo.viewportHeight - this.mTargetBounds.bottom, this.mTargetBounds.width(), this.mTargetBounds.height());

        GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, Math.abs(this.mClipBounds.left - this.mSourceBounds.left), Math.abs(this.mClipBounds.bottom - this.mSourceBounds.bottom),
                this.mTargetBounds.left, mInfo.viewportHeight - mTargetBounds.bottom, this.mTargetBounds.width(), this.mTargetBounds.height());

    }

    public void initSourceBounds(int left, int top, int right, int bottom) {
        mSourceBounds.set(left, top, right, bottom);
    }

    public static void view2Window(float[] m, Rect src, Rect dst) {
        if(dst != null) {
            float left = (float)src.left + m[12];
            float top = (float)src.top + m[13];
            float right = (float)src.right + m[12];
            float bottom = (float)src.bottom + m[13];
            dst.set((int)(left + 0.5F), (int)(top + 0.5F), (int)(right + 0.5F), (int)(bottom + 0.5F));
        }
    }

    public static void window2View(float[] m, float l, float t, float r, float b, Rect dst) {
        if(dst != null) {
            float left = l - m[12];
            float top = t - m[13];
            float right = r - m[12];
            float bottom = b - m[13];
            dst.set((int)(left + 0.5F), (int)(top + 0.5F), (int)(right + 0.5F), (int)(bottom + 0.5F));
        }
    }

    private static String getFragmentShaderCode() {
        String str = "precision mediump float; \n" +
                "uniform vec2 uStep; \n" +
                "uniform sampler2D sTexture; \n" +
                "varying vec2 vTexCoord; \n" +
                "uniform int uVertical; \n" +
                "uniform vec4 uBounds; \n" +
                "uniform int uRadius; \n" +
                "uniform float uWeight; \n" +
                "float clip(float x,float min,float max) { \n" +
                "    if (x>max) {\n" +
                "       x=max;  \n" +
                "    } else if (x<min) {\n" +
                "       x=min;  \n" +
                "    }\n" +
                "    return x;  \n" +
                "} \n" +
                "vec2 getTexCoord(vec2 texcoord,vec2 step,float n) { \n" +
                "\n" +
                "\treturn vec2(clip(texcoord.x+step.x,uBounds.x,uBounds.y), clip(texcoord.y+step.y,uBounds.z,uBounds.w));\n" +
                "\n" +
                "} \n" +
                "vec3 gassian(vec2 step) { \n" +
                "    if (uRadius == 0) return texture2D(sTexture, vTexCoord).rgb; \n" +
                "    vec3 sum = vec3(0.0, 0.0, 0.0); \n" +
                "    float j=0.0;  \n" +
                "    for (int i=0; i<=uRadius; ++i) {  \n" +
                "        if (i == 0) { \n" +
                "           sum += texture2D(sTexture, vTexCoord).rgb * uWeight; \n" +
                "        } else {  \n" +
                "           sum += texture2D(sTexture, getTexCoord(vTexCoord, step, -j)).rgb * uWeight;\n" +
                "           sum += texture2D(sTexture, getTexCoord(vTexCoord, step, j)).rgb * uWeight;\n" +
                "        }\n" +
                "        j += 1.0;\n" +
                "    }\n" +
                "    return sum; \n" +
                "} \n" +
                "void main() { \n" +
                "    gl_FragColor.rgb = gassian(uStep); \n" +
                "    gl_FragColor.a = 1.0; \n" +
                "} ";
        return str;
    }

}
