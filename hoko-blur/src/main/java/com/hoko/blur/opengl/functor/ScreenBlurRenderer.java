package com.hoko.blur.opengl.functor;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.ColorInt;
import android.util.Log;

import com.hoko.blur.HokoBlur;
import com.hoko.blur.anno.Mode;
import com.hoko.blur.api.IFrameBuffer;
import com.hoko.blur.api.IProgram;
import com.hoko.blur.api.IRenderer;
import com.hoko.blur.api.ITexture;
import com.hoko.blur.opengl.cache.FrameBufferCache;
import com.hoko.blur.opengl.cache.TextureCache;
import com.hoko.blur.opengl.program.ProgramFactory;
import com.hoko.blur.util.ColorUtil;
import com.hoko.blur.util.Preconditions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import static com.hoko.blur.util.ShaderUtil.checkGLError;
import static com.hoko.blur.util.ShaderUtil.getCopyFragmentCode;
import static com.hoko.blur.util.ShaderUtil.getFragmentShaderCode;
import static com.hoko.blur.util.ShaderUtil.getVertexCode;

/**
 * Created by yuxfzju on 16/11/23.
 */
public class ScreenBlurRenderer implements IRenderer<DrawFunctor.GLInfo> {

    private static final String TAG = ScreenBlurRenderer.class.getSimpleName();

    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private static final float squareCoords[] = {
            0.0F, 0.0F, 0.0F, // top left
            1.0F, 0.0F, 0.0F, // bottom left
            0.0F, 1.0F, 0.0F, // bottom right
            1.0F, 1.0F, 0.0F, // top right
    };

    private static final float mTexHorizontalCoords[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private static final short drawOrder[] = {0, 1, 2, 2, 3, 1};


    private int mRadius;

    @Mode
    private int mMode;

    private float mSampleFactor;

    @ColorInt
    private int mMixColor;
    private float mMixPercent;

    private volatile boolean mNeedRelink = true;
    private DrawFunctor.GLInfo mInfo;
    private volatile boolean isChildRedraw;

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

    private IProgram mBlurProgram;
    private IProgram mCopyProgram;

    private ITexture mHorizontalTexture;
    private ITexture mVerticalTexture;
    private ITexture mDisplayTexture;
    private ITexture mParentDisplayTexture;

    private IFrameBuffer mDisplayFrameBuffer;
    private IFrameBuffer mHorizontalFrameBuffer;
    private IFrameBuffer mVerticalFrameBuffer;

    private ScreenBlurRenderer(Builder builder) {
        mMode = builder.mode;
        mRadius = builder.radius;
        mSampleFactor = builder.sampleFactor;
        mMixColor = builder.mixColor;
        mMixPercent = builder.mixPercent;

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
        onDrawFrame(info, false);
    }

    public void onDrawFrame(DrawFunctor.GLInfo info, boolean isChildRedraw) {
        mInfo = checkClipSize(info);
        this.isChildRedraw = isChildRedraw;

        mWidth = info.clipRight - info.clipLeft;
        mHeight = info.clipBottom - info.clipTop;

        mScaleW = (int) (mWidth / mSampleFactor);
        mScaleH = (int) (mHeight / mSampleFactor);

        if (mWidth <= 0 || mHeight <= 0 || mScaleW <= 0 || mScaleH <= 0) {
            return;
        }

        Preconditions.checkArgument(checkBlurSize(mWidth, mHeight), "Too large blur size, check width < 1800 and height < 3200");

        try {
            //In order to provide cache for the next blur operation, call prepare() even when radius = 0.
            //Otherwise, a black screen may appear
            if (!prepare()) {
                Log.e(TAG, "OpenGL runtime prepare error");
                return;
            }
            selectDisplayTexture(isChildRedraw);
            if (mRadius > 0) {
                drawOneDimenBlur(mMVPMatrix, mTexMatrix, true);
                drawOneDimenBlur(mMVPMatrix, mTexMatrix, false);
                upscaleWithMixColor(mScreenMVPMatrix, mTexMatrix);
            }
        } finally {
            onPostBlur();
        }
    }


    private boolean checkBlurSize(int width, int height) {
        return width <= 1800 && height <= 3200;
    }

    private DrawFunctor.GLInfo checkClipSize(DrawFunctor.GLInfo info) {
        if (info.viewportHeight != 0 && (info.clipBottom - info.clipTop >= info.viewportHeight)) {
            info.clipTop = info.clipTop + 1;
        }
        if (info.viewportWidth != 0 && (info.clipRight - info.clipLeft >= info.viewportWidth)) {
            info.clipLeft = info.clipLeft + 1;
        }
        return info;
    }

    private boolean prepare() {
        EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        if (context.equals(EGL10.EGL_NO_CONTEXT)) {
            Log.e(TAG, "This thread has no EGLContext.");
            return false;
        }

        initMVPMatrix(mInfo);

        if (mNeedRelink) {
            deletePrograms();
            mBlurProgram = ProgramFactory.create(getVertexCode(), getFragmentShaderCode(mMode));
            mCopyProgram = ProgramFactory.create(getVertexCode(), getCopyFragmentCode());
            mNeedRelink = false;
        }


        if (mBlurProgram.id() == 0 || mCopyProgram.id() == 0) {
            return false;
        }

        GLES20.glClearColor(1f, 1f, 1f, 1f);
        //scissor test is enabled by default
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);   // 启用Alpha测试

        if (!isChildRedraw) {
            mDisplayTexture = TextureCache.getInstance().getTexture(mWidth, mHeight);
        }
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
        Matrix.setIdentityM(mTexMatrix, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, mScaleW, mScaleH, 1.0f);
        Matrix.orthoM(mProjMatrix, 0, 0, mScaleW, 0, mScaleH, -100f, 100f);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, info.clipLeft, info.viewportHeight - info.clipBottom, 0);
        Matrix.scaleM(mModelMatrix, 0, mWidth, mHeight, 1f);
        Matrix.setIdentityM(mProjMatrix, 0);
        Matrix.orthoM(mProjMatrix, 0, 0, info.viewportWidth, 0, info.viewportHeight, -100f, 100f);
        Matrix.multiplyMM(mScreenMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mScreenMVPMatrix, 0, mProjMatrix, 0, mScreenMVPMatrix, 0);
    }


    private void drawOneDimenBlur(float[] mvpMatrix, float[] texMatrix, boolean isHorizontal) {
        try {
            GLES20.glViewport(0, 0, mScaleW, mScaleH);

            GLES20.glUseProgram(mBlurProgram.id());
//
            int positionId = GLES20.glGetAttribLocation(mBlurProgram.id(), "aPosition");
            GLES20.glEnableVertexAttribArray(positionId);
            GLES20.glVertexAttribPointer(positionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

            int mvpMatrixId = GLES20.glGetUniformLocation(mBlurProgram.id(), "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, mvpMatrix, 0);

            int texMatrixId = GLES20.glGetUniformLocation(mBlurProgram.id(), "uTexMatrix");
            GLES20.glUniformMatrix4fv(texMatrixId, 1, false, texMatrix, 0);

            int texCoordId = GLES20.glGetAttribLocation(mBlurProgram.id(), "aTexCoord");
            GLES20.glEnableVertexAttribArray(texCoordId);
            GLES20.glVertexAttribPointer(texCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

            if (isHorizontal) {
                mHorizontalFrameBuffer.bindSelf();
            } else {
                mVerticalFrameBuffer.bindSelf();
            }

            int textureUniformId = GLES20.glGetUniformLocation(mBlurProgram.id(), "uTexture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? mDisplayTexture.id() : mHorizontalTexture.id());
            GLES20.glUniform1i(textureUniformId, 0);

            int radiusId = GLES20.glGetUniformLocation(mBlurProgram.id(), "uRadius");
            int widthOffsetId = GLES20.glGetUniformLocation(mBlurProgram.id(), "uWidthOffset");
            int heightOffsetId = GLES20.glGetUniformLocation(mBlurProgram.id(), "uHeightOffset");

            GLES20.glUniform1i(radiusId, mRadius);
            GLES20.glUniform1f(widthOffsetId, isHorizontal ? 1f / mWidth * mSampleFactor : 0);
            GLES20.glUniform1f(heightOffsetId, isHorizontal ? 0 : 1f / mHeight * mSampleFactor);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        } finally {
            reset();
        }

    }


    private void upscaleWithMixColor(float[] mvpMatrix, float[] texMatrix) {
        try {
            GLES20.glViewport(0, 0, mInfo.viewportWidth, mInfo.viewportHeight);

            GLES20.glUseProgram(mCopyProgram.id());

            int positionId = GLES20.glGetAttribLocation(mCopyProgram.id(), "aPosition");
            GLES20.glEnableVertexAttribArray(positionId);
            GLES20.glVertexAttribPointer(positionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

            int mvpMatrixId = GLES20.glGetUniformLocation(mCopyProgram.id(), "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, mvpMatrix, 0);

            int texMatrixId = GLES20.glGetUniformLocation(mCopyProgram.id(), "uTexMatrix");
            GLES20.glUniformMatrix4fv(texMatrixId, 1, false, texMatrix, 0);

            int texCoordId = GLES20.glGetAttribLocation(mCopyProgram.id(), "aTexCoord");
            GLES20.glEnableVertexAttribArray(texCoordId);
            GLES20.glVertexAttribPointer(texCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

            int mixPercentId = GLES20.glGetUniformLocation(mCopyProgram.id(), "mixPercent");
            GLES20.glUniform1f(mixPercentId, mMixPercent);

            int mixColorId = GLES20.glGetUniformLocation(mCopyProgram.id(), "vMixColor");
            GLES20.glUniform4fv(mixColorId, 1, ColorUtil.toRgbaFloatComponents(mMixColor), 0);

            mDisplayFrameBuffer.bindSelf();

            int textureUniformId = GLES20.glGetUniformLocation(mCopyProgram.id(), "uTexture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVerticalTexture.id());
            GLES20.glUniform1i(textureUniformId, 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        } finally {
            reset();
        }
    }

    private void reset() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        mVertexBuffer.rewind();
        mTexCoordBuffer.rewind();
        mDrawListBuffer.rewind();
    }


    private void selectDisplayTexture(boolean isChild) {
        ITexture parent = mParentDisplayTexture;
        ITexture current = mDisplayTexture;
        if (!isChild) {
            mParentDisplayTexture = null;
            TextureCache.getInstance().recycleTexture(parent);
            copyTextureFromScreen(mInfo);
            mParentDisplayTexture = current;
        } else {
            Preconditions.checkArgument(parent != null
                    && parent.width() == mWidth
                    && parent.height() == mHeight, "The cached texture sizes do not match");

            mDisplayTexture = null;
            TextureCache.getInstance().recycleTexture(current);
            mDisplayTexture = parent;
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
        if (mDisplayFrameBuffer != null) {
            mDisplayFrameBuffer.bindSelf();
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

        TextureCache.getInstance().recycleTexture(mHorizontalTexture);
        TextureCache.getInstance().recycleTexture(mVerticalTexture);

        FrameBufferCache.getInstance().recycleFrameBuffer(mHorizontalFrameBuffer);
        FrameBufferCache.getInstance().recycleFrameBuffer(mVerticalFrameBuffer);
    }

    public void free() {
        deletePrograms();
        mNeedRelink = true;
    }

    private void deletePrograms() {
        if (mBlurProgram != null) {
            mBlurProgram.delete();
        }

        if (mCopyProgram != null) {
            mCopyProgram.delete();
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

    public void mixColor(@ColorInt int mixColor) {
        mMixColor = mixColor;
    }

    public void mixPercent(float mixPercent) {
        mMixPercent = mixPercent;
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

    @ColorInt
    public int mixColor() {
        return mMixColor;
    }

    public float mixPercent() {
        return mMixPercent;
    }

    public static class Builder {
        @Mode
        private int mode = HokoBlur.MODE_STACK;
        private int radius = 5;
        private float sampleFactor = 4.0f;
        @ColorInt
        private int mixColor = Color.TRANSPARENT;
        private float mixPercent = 1.0f;

        public Builder() {

        }

        public Builder(ScreenBlurRenderer screenRenderer) {
            Preconditions.checkNotNull(screenRenderer, "ScreenBlurRenderer == null");
            this.mode = screenRenderer.mode();
            this.radius = screenRenderer.radius();
            this.sampleFactor = screenRenderer.sampleFactor();
        }

        public Builder mode(int mode) {
            this.mode = mode;
            return this;
        }

        public Builder radius(int radius) {
            this.radius = radius;
            return this;

        }

        public Builder sampleFactor(float sampleFactor) {
            this.sampleFactor = sampleFactor;
            return this;
        }

        public Builder mixColor(@ColorInt int mixColor) {
            this.mixColor = mixColor;
            return this;
        }

        public Builder mixPercent(float mixPercent) {
            Preconditions.checkArgument(mixPercent <= 1.0f && mixPercent >= 0, "set 0 <= mixPercent <= 1.0f");
            this.mixPercent = mixPercent;
            return this;
        }

        public ScreenBlurRenderer build() {
            return new ScreenBlurRenderer(this);
        }
    }


}
