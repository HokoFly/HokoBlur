package com.hoko.blurlibrary.opengl.functor;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.anno.Mode;
import com.hoko.blurlibrary.api.IFrameBuffer;
import com.hoko.blurlibrary.api.IScreenRenderer;
import com.hoko.blurlibrary.api.ITexture;
import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.opengl.cache.TextureCache;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import static com.hoko.blurlibrary.util.ShaderUtil.checkGLError;
import static com.hoko.blurlibrary.util.ShaderUtil.createProgram;
import static com.hoko.blurlibrary.util.ShaderUtil.getCopyFragmentCode;
import static com.hoko.blurlibrary.util.ShaderUtil.getFragmentShaderCode;
import static com.hoko.blurlibrary.util.ShaderUtil.getVertexCode;

/**
 * Created by yuxfzju on 16/11/23.
 */
public class ScreenBlurRenderer implements IScreenRenderer {

    private static final String TAG = "ScreenBlurRenderer";

    @Mode
    private static final int DEFAULT_MODE = Blur.MODE_GAUSSIAN;
    private static final int DEFAULT_BLUR_RADIUS = 5;
    private static final float DEFAULT_SAMPLE_FACTOR = 4.0f;

    private int mRadius = DEFAULT_BLUR_RADIUS;

    @Mode
    private int mMode = DEFAULT_MODE;

    private float mSampleFactor = DEFAULT_SAMPLE_FACTOR;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mScreenMVPMatrix = new float[16];

    private float[] mTexMatrix = new float[16];

    private int mWidth;
    private int mHeight;

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

    private int mBlurProgram;
    private int mCopyProgram;

    private int mPositionId;
    private int mMVPMatrixId;
    private int mTexCoordId;
    private int mTexMatrixId;

    private int mTextureUniformId;

    private ITexture mHorizontalTexture;
    private ITexture mVerticalTexture;
    private ITexture mDisplayTexture;

    private IFrameBuffer mDisplayFrameBuffer;
    private IFrameBuffer mHorizontalFrameBuffer;
    private IFrameBuffer mVerticalFrameBuffer;

    private boolean mHasEGLContext = false;
    private boolean mNeedRelink = true;
    private DrawFunctor.GLInfo mInfo;
    private TextureCache mTextureCache = TextureCache.getInstance();
    private FrameBufferCache mFrameBufferCache = FrameBufferCache.getInstance();

    public ScreenBlurRenderer() {
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


    @Override
    public void onDrawFrame(DrawFunctor.GLInfo info) {
        mInfo = info;

        mWidth = info.clipRight - info.clipLeft;
        mHeight = info.clipBottom - info.clipTop;
        
        mScaleW = (int) (mWidth / mSampleFactor);
        mScaleH = (int) (mHeight / mSampleFactor);

        if (mWidth <= 0 || mHeight <= 0 || mScaleW <= 0 || mScaleH <= 0) {
            return;
        }

        if (!prepare()) { //渲染环境错误返回
            Log.e(TAG, "onDrawFrame: prepare");
            return;
        }

        //半径为0也需要调用prepare()，这是为了下次模糊提供缓存，
        // 否则在做动画时，半径从0到1时，会因为需要prepare的额外耗时，使得Drawable出现短暂的黑屏
        if (mRadius > 0) {
            copyTextureFromScreen(mInfo);
            getTexMatrix(false);
            drawOneDimenBlur(mMVPMatrix, mTexMatrix, true);
            drawOneDimenBlur(mMVPMatrix, mTexMatrix, false);
            getTexMatrix(true);
            upscale(mScreenMVPMatrix, mTexMatrix);
        }

        onPostBlur();
    }

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        //surface尺寸始终为整块屏幕
    }

    private boolean checkBlurSize(int width, int height) {
        return width <= 1800 && height <= 3200;
    }

    private boolean prepare() {
        if (!mHasEGLContext) {
            EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
            if (context.equals(EGL10.EGL_NO_CONTEXT)) {
                Log.e(TAG, "This thread is no EGLContext.");
                return false;
            }

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            mHasEGLContext = true;
        }

        if (!checkBlurSize(mWidth, mHeight)) {
            return false;
        }

        initMVPMatrix(mInfo);

        if (mNeedRelink) {
            deletePrograms();
            mBlurProgram = createProgram(getVertexCode(), getFragmentShaderCode(mMode));
            mCopyProgram = createProgram(getVertexCode(), getCopyFragmentCode());
            mNeedRelink = false;
        }


        if (mBlurProgram == 0 || mCopyProgram == 0) {
            return false;
        }

        GLES20.glClearColor(1f, 1f, 1f, 1f);
        // fuck scissor leads to bugfix for one week !!
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        mDisplayTexture = mTextureCache.getTexture(mWidth, mHeight);
        mHorizontalTexture = mTextureCache.getTexture(mScaleW, mScaleH);
        mVerticalTexture = mTextureCache.getTexture(mScaleW, mScaleH);

        mDisplayFrameBuffer = mFrameBufferCache.getDisplayFrameBuffer();
        mHorizontalFrameBuffer = mFrameBufferCache.getFrameBuffer();
        mVerticalFrameBuffer = mFrameBufferCache.getFrameBuffer();
        if (mHorizontalFrameBuffer != null) {
            mHorizontalFrameBuffer.bindTexture(mHorizontalTexture);
        }
        if (mVerticalFrameBuffer != null) {
            mVerticalFrameBuffer.bindTexture(mVerticalTexture);
        }

        return checkGLError("Prepare to blurring");

    }


    /**
     * MVP的取值
     *  Model                            View           Projection
     * transform + scaled Width&Height   Identity       viewport
     * scaled Width&Height               Identity       scaled Width&Height
     */
    private void initMVPMatrix(DrawFunctor.GLInfo info) {
        if (info == null) {
            return;
        }

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
    }


    private void drawOneDimenBlur(float[] mvpMatrix, float[] texMatrix, boolean isHorizontal) {
        GLES20.glViewport(0, 0, mScaleW, mScaleH);

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

        if (isHorizontal) {
            mHorizontalFrameBuffer.bindSelf();
        } else {
            mVerticalFrameBuffer.bindSelf();
        }

        mTextureUniformId = GLES20.glGetUniformLocation(mBlurProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? mDisplayTexture.id() : mHorizontalTexture.id());
        GLES20.glUniform1i(mTextureUniformId, 0);

        int radiusId = GLES20.glGetUniformLocation(mBlurProgram, "uRadius");
        int widthOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uWidthOffset");
        int heightOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uHeightOffset");

        GLES20.glUniform1i(radiusId, mRadius);
        GLES20.glUniform1f(widthOffsetId, isHorizontal ? 1f / mWidth * mSampleFactor : 0);
        GLES20.glUniform1f(heightOffsetId, isHorizontal ? 0 : 1f / mHeight * mSampleFactor);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);


        resetAllBuffer();

    }


    private void upscale(float[] mvpMatrix, float[] texMatrix) {
        GLES20.glViewport(0, 0, mInfo.viewportWidth, mInfo.viewportHeight);

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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVerticalTexture.id());
        GLES20.glUniform1i(mTextureUniformId, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        resetAllBuffer();


    }

    private void resetAllBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        mVertexBuffer.rewind();
        mTexCoordBuffer.rewind();
        mDrawListBuffer.rewind();
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

    private void copyTextureFromScreen(DrawFunctor.GLInfo info) {
        if (info != null && mDisplayTexture != null && mDisplayTexture.id() != 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDisplayTexture.id());
            GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, info.clipLeft, info.viewportHeight - info.clipBottom, mWidth, mHeight);
        }

    }


    private void onPostBlur() {
        mDisplayFrameBuffer.bindSelf();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

        mTextureCache.recycleTexture(mDisplayTexture);
        mTextureCache.recycleTexture(mHorizontalTexture);
        mTextureCache.recycleTexture(mVerticalTexture);

        mFrameBufferCache.recycleFrameBuffer(mHorizontalFrameBuffer);
        mFrameBufferCache.recycleFrameBuffer(mVerticalFrameBuffer);
    }

    @Override
    public void free() {
        mTextureCache.deleteTextures();
        mFrameBufferCache.deleteFrameBuffers();
        GLES20.glDisableVertexAttribArray(mPositionId);
        GLES20.glDisableVertexAttribArray(mTexCoordId);
        deletePrograms();
        mNeedRelink = true;
    }

    private void deletePrograms() {
        if (mBlurProgram != 0) {
            GLES20.glDeleteProgram(mBlurProgram);
        }
        if (mCopyProgram != 0) {
            GLES20.glDeleteProgram(mCopyProgram);
        }
    }

    @Override
    public void mode(@Mode int mode) {
        mMode = mode;
        mNeedRelink = true;
    }

    @Override
    public void radius(int radius) {
        mRadius = radius;
    }

    @Override
    public void sampleFactor(float factor) {
        mSampleFactor = factor;
    }

    @Override
    public int mode() {
        return mMode;
    }

    @Override
    public int radius() {
        return mRadius;
    }

    @Override
    public float sampleFactor() {
        return mSampleFactor;
    }

}
