package com.hoko.blur.opengl.functor;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.anno.Mode;
import com.hoko.blur.api.IFrameBuffer;
import com.hoko.blur.api.IRenderer;
import com.hoko.blur.api.ITexture;
import com.hoko.blur.opengl.cache.FrameBufferCache;
import com.hoko.blur.opengl.cache.TextureCache;
import com.hoko.blur.util.Preconditions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import static com.hoko.blur.util.ShaderUtil.checkGLError;
import static com.hoko.blur.util.ShaderUtil.createProgram;
import static com.hoko.blur.util.ShaderUtil.getCopyFragmentCode;
import static com.hoko.blur.util.ShaderUtil.getFragmentShaderCode;
import static com.hoko.blur.util.ShaderUtil.getVertexCode;

/**
 * Created by yuxfzju on 16/11/23.
 */
public class ScreenBlurRenderer implements IRenderer<DrawFunctor.GLInfo> {

    private static final String TAG = "ScreenBlurRenderer";

    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private static final float squareCoords[] = {
            0.0F, 0.0F, 0.0F, // top left
            1.0F, 0.0F, 0.0F, // bottom left
            0.0F, 1.0F, 0.0F, // bottom right
            1.0F, 1.0F, 0.0F}; // top right

    private static final float mTexHorizontalCoords[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f};

    private static final short drawOrder[] = {0, 1, 2, 2, 3, 1};


    private int mRadius;

    @Mode
    private int mMode;

    private float mSampleFactor;

    private volatile boolean mNeedRelink = true;
    private DrawFunctor.GLInfo mInfo;

    private int mWidth;
    private int mHeight;

    private int mScaleW;
    private int mScaleH;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mScreenMVPMatrix = new float[16];

    private float[] mTexMatrix = new float[16];

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mTexCoordBuffer;

    private int mBlurProgram;
    private int mCopyProgram;

    private int mPositionId;
    private int mMVPMatrixId;
    private int mTexCoordId;
    private int mTexMatrixId;

    private ITexture mHorizontalTexture;
    private ITexture mVerticalTexture;
    private ITexture mDisplayTexture;

    private IFrameBuffer mDisplayFrameBuffer;
    private IFrameBuffer mHorizontalFrameBuffer;
    private IFrameBuffer mVerticalFrameBuffer;

    private ScreenBlurRenderer(Builder builder) {
        mMode = builder.mode;
        mRadius = builder.radius;
        mSampleFactor = builder.sampleFactor;

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

    public Builder newBuilder() {
        return new Builder(this);
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
            try {
                copyTextureFromScreen(mInfo);
                getTexMatrix(false);
                drawOneDimenBlur(mMVPMatrix, mTexMatrix, true);
                drawOneDimenBlur(mMVPMatrix, mTexMatrix, false);
                getTexMatrix(true);
                upscale(mScreenMVPMatrix, mTexMatrix);
            } finally {
                onPostBlur();
            }

        }
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
        EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        if (context.equals(EGL10.EGL_NO_CONTEXT)) {
            Log.e(TAG, "This thread has no EGLContext.");
            return false;
        }

        Preconditions.checkArgument(checkBlurSize(mWidth, mHeight), "Too large blurred sizes");

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

        mDisplayTexture = TextureCache.getInstance().getTexture(mWidth, mHeight);
        mHorizontalTexture = TextureCache.getInstance().getTexture(mScaleW, mScaleH);
        mVerticalTexture = TextureCache.getInstance().getTexture(mScaleW, mScaleH);

        mDisplayFrameBuffer = FrameBufferCache.getInstance().getDisplayFrameBuffer();
        mHorizontalFrameBuffer = FrameBufferCache.getInstance().getFrameBuffer();
        mVerticalFrameBuffer = FrameBufferCache.getInstance().getFrameBuffer();
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
     * Model                            View           Projection
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
        try {
            GLES20.glViewport(0, 0, mScaleW, mScaleH);

            GLES20.glUseProgram(mBlurProgram);
//
            mPositionId = GLES20.glGetAttribLocation(mBlurProgram, "aPosition");
            GLES20.glEnableVertexAttribArray(mPositionId);
            GLES20.glVertexAttribPointer(mPositionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

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

            int textureUniformId = GLES20.glGetUniformLocation(mBlurProgram, "uTexture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? mDisplayTexture.id() : mHorizontalTexture.id());
            GLES20.glUniform1i(textureUniformId, 0);

            int radiusId = GLES20.glGetUniformLocation(mBlurProgram, "uRadius");
            int widthOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uWidthOffset");
            int heightOffsetId = GLES20.glGetUniformLocation(mBlurProgram, "uHeightOffset");

            GLES20.glUniform1i(radiusId, mRadius);
            GLES20.glUniform1f(widthOffsetId, isHorizontal ? 1f / mWidth * mSampleFactor : 0);
            GLES20.glUniform1f(heightOffsetId, isHorizontal ? 0 : 1f / mHeight * mSampleFactor);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        } finally {
            resetAllBuffer();
        }

    }


    private void upscale(float[] mvpMatrix, float[] texMatrix) {
        GLES20.glViewport(0, 0, mInfo.viewportWidth, mInfo.viewportHeight);

        GLES20.glUseProgram(mCopyProgram);

        mPositionId = GLES20.glGetAttribLocation(mCopyProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionId);
        GLES20.glVertexAttribPointer(mPositionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

        mMVPMatrixId = GLES20.glGetUniformLocation(mCopyProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixId, 1, false, mvpMatrix, 0);

        mTexMatrixId = GLES20.glGetUniformLocation(mCopyProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(mTexMatrixId, 1, false, texMatrix, 0);

        mTexCoordId = GLES20.glGetAttribLocation(mCopyProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordId);
        GLES20.glVertexAttribPointer(mTexCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        mDisplayFrameBuffer.bindSelf();

        int textureUniformId = GLES20.glGetUniformLocation(mCopyProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVerticalTexture.id());
        GLES20.glUniform1i(textureUniformId, 0);

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

        TextureCache.getInstance().recycleTexture(mDisplayTexture);
        TextureCache.getInstance().recycleTexture(mHorizontalTexture);
        TextureCache.getInstance().recycleTexture(mVerticalTexture);

        FrameBufferCache.getInstance().recycleFrameBuffer(mHorizontalFrameBuffer);
        FrameBufferCache.getInstance().recycleFrameBuffer(mVerticalFrameBuffer);
    }

    @Override
    public void free() {
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

    public void mode(@Mode int mode) {
        mMode = mode;
        mNeedRelink = true;
    }

    public void radius(int radius) {
        mRadius = radius;
    }

    public void sampleFactor(float factor) {
        mSampleFactor = factor;
    }

    public int mode() {
        return mMode;
    }

    public int radius() {
        return mRadius;
    }

    public float sampleFactor() {
        return mSampleFactor;
    }


    public static class Builder {
        @Mode
        private int mode = HokoBlur.MODE_GAUSSIAN;
        private int radius = 5;
        private float sampleFactor = 4.0f;

        public Builder() {

        }

        public Builder(ScreenBlurRenderer screenRenderer) {
            Preconditions.checkNotNull(screenRenderer, "ScreenBlurRenderer == null");
            this.mode = screenRenderer.mode();
            this.radius = screenRenderer.radius();
            this.sampleFactor = screenRenderer.sampleFactor();
        }

        public void mode(int mode) {
            this.mode = mode;
        }

        public void radius(int radius) {
            this.radius = radius;
        }

        public void sampleFactor(float sampleFactor) {
            this.sampleFactor = sampleFactor;
        }

        public ScreenBlurRenderer build() {
            return new ScreenBlurRenderer(this);
        }
    }


}
