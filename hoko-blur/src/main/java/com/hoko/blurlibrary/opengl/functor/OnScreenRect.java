package com.hoko.blurlibrary.opengl.functor;

import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.opengl.cache.TextureCache;
import com.hoko.blurlibrary.opengl.framebuffer.IFrameBuffer;
import com.hoko.blurlibrary.opengl.texture.ITexture;
import com.hoko.blurlibrary.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import static com.hoko.blurlibrary.util.ShaderUtil.getCopyFragmentCode;
import static com.hoko.blurlibrary.util.ShaderUtil.getFragmentShaderCode;
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

    private ITexture mHorizontalTexture;
    private ITexture mVerticalTexture;
    private ITexture mDisplayTexture;

    private IFrameBuffer mDisplayFrameBuffer;
    private IFrameBuffer mHorizontalFrameBuffer;
    private IFrameBuffer mVerticalFrameBuffer;

    private boolean mInited = false;
    private DrawFunctor.GLInfo mInfo;
    private TextureCache mTextureCache = TextureCache.getInstance();
    private FrameBufferCache mFrameBufferCache = FrameBufferCache.getInstance();
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

            mDisplayFrameBuffer = mFrameBufferCache.getDisplayFrameBuffer();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            mInited = true;

        }

        GLES20.glClearColor(1f, 1f, 1f, 1f);

        initProgram();

        // fuck scissor leads to bugfix for one week !!
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        mDisplayTexture = mTextureCache.getTexture(mWidth, mHeight);
        mHorizontalTexture = mTextureCache.getTexture(mScaleW, mScaleH);
        mVerticalTexture = mTextureCache.getTexture(mScaleW, mScaleH);

        mHorizontalFrameBuffer = mFrameBufferCache.getFrameBuffer();
        if (mHorizontalFrameBuffer != null) {
            mHorizontalFrameBuffer.bindTexture(mHorizontalTexture);
        }

        mVerticalFrameBuffer = mFrameBufferCache.getFrameBuffer();
        if (mVerticalFrameBuffer != null) {
            mVerticalFrameBuffer.bindTexture(mVerticalTexture);
        }

        copyFBO();

        GLES20.glViewport(0, 0, mScaleW, mScaleH);
        getTexMatrix(false);
        drawHorizontalBlur(mMVPMatrix, mTexMatrix);
        drawVerticalBlur(mMVPMatrix, mTexMatrix);

        GLES20.glViewport(0, 0, info.viewportWidth, info.viewportHeight);
        getTexMatrix(true);
//        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
//        mVerticalTexture = mDisplayTexture;

        upscale(mScreenMVPMatrix, mTexMatrix);

        mDisplayFrameBuffer.bindSelf();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

        mTextureCache.recycleTexture(mDisplayTexture);
        mTextureCache.recycleTexture(mHorizontalTexture);
        mTextureCache.recycleTexture(mVerticalTexture);

        mFrameBufferCache.recycleFrameBuffer(mHorizontalFrameBuffer);
        mFrameBufferCache.recycleFrameBuffer(mVerticalFrameBuffer);

        // TODO: 2017/1/22 统一delete
//        GLES20.glDeleteTextures(3, new int[]{mDisplayTexture, mHorizontalTexture, mVerticalTexture}, 0);
//        GLES20.glDeleteFramebuffers(2, new int[]{mHorizontalFrameBuffer, mVerticalFrameBuffer}, 0);

        GLES20.glDeleteProgram(mBlurProgram);
        GLES20.glDeleteProgram(mCopyProgram);

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

        mTexCoordId = GLES20.glGetAttribLocation(mBlurProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordId);
        GLES20.glVertexAttribPointer(mTexCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        mHorizontalFrameBuffer.bindSelf();

        mTextureUniformId = GLES20.glGetUniformLocation(mBlurProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDisplayTexture.getId());
        GLES20.glUniform1i(mTextureUniformId, 0);

        mWidthOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uWidthOffset");
        mHeightOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetId, 1f / mWidth / FACTOR);
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

//        mTexCoordId = GLES20.glGetAttribLocation(mBlurProgram, "aTexCoord");
//        GLES20.glEnableVertexAttribArray(mTexCoordId);
        GLES20.glVertexAttribPointer(mTexCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        mVerticalFrameBuffer.bindSelf();

//        mTextureUniformId = GLES20.glGetUniformLocation(mBlurProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHorizontalTexture.getId());
        GLES20.glUniform1i(mTextureUniformId, 0);

        mWidthOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uWidthOffset");
        mHeightOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uHeightOffset");
        GLES20.glUniform1f(mWidthOffsetId, 0f / mWidth / FACTOR);
        GLES20.glUniform1f(mHeightOffsetId, 1f / mHeight / FACTOR);

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

        mDisplayFrameBuffer.bindSelf();

        mTextureUniformId = GLES20.glGetUniformLocation(mCopyProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVerticalTexture.getId());
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



    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


    private void getTexMatrix(boolean flipY) {
        Matrix.setIdentityM(mTexMatrix, 0);

        if (flipY) {
            Matrix.translateM(mTexMatrix, 0, 0, 1.0f, 0);
            Matrix.scaleM(mTexMatrix, 0, 1.0f, -1.0f, 1.0F);
        } else {
            Matrix.translateM(mTexMatrix, 0, 0, 0, 0);
            Matrix.scaleM(mTexMatrix, 0, 1.0f, 1.0f, 1.0F);
        }
    }

    private void copyFBO() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDisplayTexture.getId());
        GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mInfo.clipLeft, mInfo.viewportHeight - mInfo.clipBottom, mWidth, mHeight);

    }

    public void destroy() {
        GLES20.glDisableVertexAttribArray(mPositionId);
        GLES20.glDisableVertexAttribArray(mTexCoordId);
    }



}
