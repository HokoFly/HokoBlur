package com.hoko.blurlibrary.opengl.functor;

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

import static com.hoko.blurlibrary.util.ShaderUtil.getCopyFragmentCode;
import static com.hoko.blurlibrary.util.ShaderUtil.getVetexCode;

/**
 * Created by xiangpi on 16/11/23.
 */
public class OnScreenRect {

    private static final String TAG = "OnScreenRect";

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mScreenMVPMatrix = new float[16];

    private float[] mTexMatrix = new float[16];

    private int mWidth;
    private int mHeight;

    private static final float FACTOR = 0.25f;

    private RectF mBound1;
    private RectF mBound2;

    private int mScaleW;
    private int mScaleH;

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

    private short drawOrder[] = {0, 1, 2, 2, 3, 1};

    private int mVertexStride = COORDS_PER_VERTEX * 4;

    private int mVertexShader;
    private int mBlurFragmentShader;
    private int mCopyFragmentShader;

    private int mBlurProgram;
    private int mCopyProgram;

    private int mPositionId;
    private int mMVPMatrixId;
    private int mTexCoordId;
    private int mTexMatrixId;
    private int mBoundsId;

    private int mWidthOffsetId;
    private int mHeightOffsetId;
    private int mTextureUniformId;

    private int mHorizontalTexture;
    private int mVerticalTexture;
    private int mDisplayTexture;

    private int mDisplayFrameBuffer;
    private int mHorizontalFrameBuffer;
    private int mVerticalFrameBuffer;

    private boolean mInited = false;
    private DrawFunctor.GLInfo mInfo;
//    private Rect mClipBounds = new Rect();
//    private Rect mSourceBounds = new Rect();
//    private Rect mTargetBounds = new Rect();

    public OnScreenRect() {
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
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVetexCode());
        mBlurFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShaderCode(6, Blur.MODE_GAUSSIAN));
        mCopyFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getCopyFragmentCode());


        mBlurProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mBlurProgram, mVertexShader);
        GLES20.glAttachShader(mBlurProgram, mBlurFragmentShader);
        GLES20.glLinkProgram(mBlurProgram);

        mCopyProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mCopyProgram, mVertexShader);
        GLES20.glAttachShader(mCopyProgram, mCopyFragmentShader);
        GLES20.glLinkProgram(mCopyProgram);
    }

    public void handleGlInfo(DrawFunctor.GLInfo info) {
        mInfo = info;

        mWidth = info.clipRight - info.clipLeft;
        mHeight = info.clipBottom - info.clipTop;

        mScaleW = (int) (mWidth * FACTOR);
        mScaleH = (int) (mHeight * FACTOR);

        if (mWidth <= 0 || mHeight <= 0) {
            return;
        }

        if (!mInited) {
            EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
            if (context.equals(EGL10.EGL_NO_CONTEXT)) {
                Log.e(TAG, "This thread is no EGLContext.");
                return;
            }

            initSize();

            /**
             * MVP的取值
             *  Model                            View           Projection
             * transform + scaled Width&Height   Identity       viewport
             * scaled Width&Height               Identity       scaled Width&Height
             */

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mViewMatrix, 0);
            Matrix.setIdentityM(mProjMatrix, 0);
            Matrix.scaleM(mModelMatrix, 0, mScaleW, mScaleH, 1.0f);
            Matrix.orthoM(mProjMatrix, 0, 0, mScaleW, 0, mScaleH, -100f, 100f);
            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

            System.arraycopy(info.transform, 0, mModelMatrix, 0, 16);
            Matrix.scaleM(mModelMatrix, 0, mWidth, mHeight, 1f);
            Matrix.setIdentityM(mProjMatrix, 0);
            Matrix.orthoM(mProjMatrix, 0, 0, info.viewportWidth, info.viewportHeight, 0, -100f, 100f);
            Matrix.multiplyMM(mScreenMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mScreenMVPMatrix, 0, mProjMatrix, 0, mScreenMVPMatrix, 0);


            // 获得当前绑定的FBO（屏上）
            int[] displayFbo = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, displayFbo, 0);
            mDisplayFrameBuffer = displayFbo[0];

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glClearColor(1f, 1f, 1f, 1f);
            mInited = true;

        }

        initProgram();

        // fuck scissor leads to bugfix for one week !!
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        mDisplayTexture = loadTexture(mWidth, mHeight);
        mHorizontalTexture = loadTexture(mScaleW, mScaleH);
        mHorizontalFrameBuffer = genFrameBuffer(mHorizontalTexture);
        mVerticalTexture = loadTexture(mScaleW, mScaleH);
        mVerticalFrameBuffer = genFrameBuffer(mVerticalTexture);

        copyFBO();

        GLES20.glViewport(0, 0, mScaleW, mScaleH);
        getTexMatrix(false);
        drawHorizontalBlur(mMVPMatrix, mTexMatrix);
        drawVerticalBlur(mMVPMatrix, mTexMatrix);

        GLES20.glViewport(0, 0, info.viewportWidth, info.viewportHeight);
        getTexMatrix(true);
        upscale(mScreenMVPMatrix, mTexMatrix);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mDisplayFrameBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        GLES20.glDeleteTextures(3, new int[]{mDisplayTexture, mHorizontalTexture, mVerticalTexture}, 0);
        GLES20.glDeleteFramebuffers(2, new int[]{mHorizontalFrameBuffer, mVerticalFrameBuffer}, 0);
        GLES20.glDeleteProgram(mBlurProgram);
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

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return frameBufferIds[0];

    }


    private void drawHorizontalBlur(float[] mvpMatrix, float[] texMatrix) {
        GLES20.glUseProgram(mBlurProgram);
//
        mPositionId = GLES20.glGetAttribLocation(mBlurProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionId);
        GLES20.glVertexAttribPointer(mPositionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);

        mMVPMatrixId = GLES20.glGetUniformLocation(mBlurProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixId, 1, false, mvpMatrix, 0);

        mTexMatrixId = GLES20.glGetUniformLocation(mBlurProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(mTexMatrixId, 1, false, texMatrix, 0);

        mBoundsId = GLES20.glGetUniformLocation(mBlurProgram, "uBounds");
        GLES20.glUniform4f(mBoundsId, mBound1.left, mBound1.right, mBound1.top, mBound1.bottom);

        mTexCoordId = GLES20.glGetAttribLocation(mBlurProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordId);
        GLES20.glVertexAttribPointer(mTexCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mHorizontalFrameBuffer);

        mTextureUniformId = GLES20.glGetUniformLocation(mBlurProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDisplayTexture);
        GLES20.glUniform1i(mTextureUniformId, 0);

        mWidthOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uWidthOffset");
        mHeightOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetId, (mBound1.right - mBound1.left) / mWidth / FACTOR);
        GLES20.glUniform1f(mHeightOffsetId, 0f / mHeight / FACTOR);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        resetAllBuffer();

    }


    private void drawVerticalBlur(float[] mvpMatrix, float[] texMatrix) {
        GLES20.glUseProgram(mBlurProgram);

        mPositionId = GLES20.glGetAttribLocation(mBlurProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionId);
        GLES20.glVertexAttribPointer(mPositionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);

//        mMVPMatrixId = GLES20.glGetUniformLocation(mBlurProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixId, 1, false, mvpMatrix, 0);

        GLES20.glUniformMatrix4fv(mTexMatrixId, 1, false, texMatrix, 0);

        GLES20.glUniform4f(mBoundsId, mBound2.left, mBound2.right, mBound2.top, mBound2.bottom);

//        mTexCoordId = GLES20.glGetAttribLocation(mBlurProgram, "aTexCoord");
//        GLES20.glEnableVertexAttribArray(mTexCoordId);
        GLES20.glVertexAttribPointer(mTexCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mVerticalFrameBuffer);

//        mTextureUniformId = GLES20.glGetUniformLocation(mBlurProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHorizontalTexture);
        GLES20.glUniform1i(mTextureUniformId, 0);

        mWidthOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uWidthOffset");
        mHeightOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetId, 0f / mWidth / FACTOR);
        GLES20.glUniform1f(mHeightOffsetId, (mBound2.bottom - mBound2.top) / mHeight / FACTOR);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        resetAllBuffer();
    }

    private void upscale(float[] mvpMatrix, float[] texMatrix) {
        GLES20.glUseProgram(mCopyProgram);

        mPositionId = GLES20.glGetAttribLocation(mCopyProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionId);
        GLES20.glVertexAttribPointer(mPositionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);

        mMVPMatrixId = GLES20.glGetUniformLocation(mCopyProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixId, 1, false, mvpMatrix, 0);

        mTexMatrixId = GLES20.glGetUniformLocation(mCopyProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(mTexMatrixId, 1, false, texMatrix, 0);

        mTexCoordId = GLES20.glGetAttribLocation(mCopyProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordId);
        GLES20.glVertexAttribPointer(mTexCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mDisplayFrameBuffer);

        mTextureUniformId = GLES20.glGetUniformLocation(mCopyProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVerticalTexture);
        GLES20.glUniform1i(mTextureUniformId, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        resetAllBuffer();


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
//                .append("uniform vec4 uBounds;   \n")
                .append("varying vec2 vTexCoord;   \n")
                .append("uniform sampler2D uTexture;   \n")
                .append("uniform float uWidthOffset;  \n")
                .append("uniform float uHeightOffset;  \n")
                .append("mediump float getGaussWeight(mediump float currentPos, mediump float sigma) \n")
                .append("{ \n")
                .append("   return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma)); \n")
                .append("} \n")

                /**
                 * Android 4.4一下系统编译器优化，这里注释暂时不用的GLSL代码
                 */
//                .append("float clip(float x,float min,float max) { \n")
//                .append("    if (x>max) {\n")
//                .append("       x=max;  \n")
//                .append("    } else if (x<min) {\n")
//                .append("       x=min;  \n")
//                .append("    }\n")
//                .append("    return x;  \n")
//                .append("} ")
//                .append("vec2 getTexCoord(vec2 texcoord,vec2 step) { \n")
//                .append("return vec2(clip(texcoord.x+step.x,uBounds.x,uBounds.y), clip(texcoord.y+step.y,uBounds.z,uBounds.w));\n")
//                .append("} ")
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

    private void initSize() {
        int viewportW = mWidth;
        int viewportH = mHeight;

        int fboW = nextMultipleN(mWidth + 8.0f, 4);
        int fboH = nextMultipleN(mHeight + 8.0f, 4);

        float right = (float) viewportW / (float) fboW;
        float bottom = (float) viewportH / (float) fboH;
        int width = viewportW + 8;
        int height = viewportH + 8;
        float scaleX = (float) width / (float) (width - 8);
        float scaleY = (float) height / (float) (height - 8);

        mBound1 = new RectF(0, 0, scaleX * right, scaleY * bottom);
        mBound2 = new RectF(0, 0, scaleX, scaleY);
    }

    private void getTexMatrix(boolean flipY) {
        Matrix.setIdentityM(mTexMatrix, 0);

        if (flipY) {
            Matrix.translateM(mTexMatrix, 0, mBound2.left, mBound2.bottom, 0);
            Matrix.scaleM(mTexMatrix, 0, mBound2.width(), -mBound2.height(), 1.0F);
        } else {
            Matrix.translateM(mTexMatrix, 0, mBound1.left, mBound1.top, 0);
            Matrix.scaleM(mTexMatrix, 0, mBound1.width(), mBound1.height(), 1.0F);
        }
    }

    private int nextMultipleN(float value, int n) {
        return (int) (value + (float) n - 1.0F) / n * n;
    }


    private void copyFBO() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDisplayTexture);
//        window2View(mInfo.transform, (float) mInfo.clipLeft, (float) mInfo.clipTop, (float) mInfo.clipRight, (float) mInfo.clipBottom, this.mClipBounds);
//        mClipBounds.intersect(this.mSourceBounds);
//        view2Window(mInfo.transform, this.mClipBounds, this.mTargetBounds);
//        GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, Math.abs(this.mClipBounds.left - this.mSourceBounds.left), Math.abs(this.mClipBounds.bottom - this.mSourceBounds.bottom),
//                this.mTargetBounds.left, mInfo.viewportHeight - mTargetBounds.bottom, this.mTargetBounds.width(), this.mTargetBounds.height());
        GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mInfo.clipLeft, mInfo.viewportHeight - mInfo.clipBottom, mWidth, mHeight);

    }

    public void destroy() {
        GLES20.glDisableVertexAttribArray(mPositionId);
        GLES20.glDisableVertexAttribArray(mTexCoordId);
    }

//    public void initSourceBounds(int left, int top, int right, int bottom) {
//        mSourceBounds.set(left, top, right, bottom);
//    }

//    public static void view2Window(float[] m, Rect src, Rect dst) {
//        if (dst != null) {
//            float left = (float) src.left + m[12];
//            float top = (float) src.top + m[13];
//            float right = (float) src.right + m[12];
//            float bottom = (float) src.bottom + m[13];
//            dst.set((int) (left + 0.5F), (int) (top + 0.5F), (int) (right + 0.5F), (int) (bottom + 0.5F));
//        }
//    }
//
//    public static void window2View(float[] m, float l, float t, float r, float b, Rect dst) {
//        if (dst != null) {
//            float left = l - m[12];
//            float top = t - m[13];
//            float right = r - m[12];
//            float bottom = b - m[13];
//            dst.set((int) (left + 0.5F), (int) (top + 0.5F), (int) (right + 0.5F), (int) (bottom + 0.5F));
//        }
//    }




}
