package com.hoko.blurlibrary.opengl.offscreen;

import android.graphics.Bitmap;
import android.hardware.camera2.params.Face;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.util.Log;


import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.opengl.cache.TextureCache;
import com.hoko.blurlibrary.opengl.framebuffer.IFrameBuffer;
import com.hoko.blurlibrary.opengl.texture.ITexture;
import com.hoko.blurlibrary.opengl.texture.TextureFactory;
import com.hoko.blurlibrary.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.hoko.blurlibrary.util.ShaderUtil.loadShader;

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

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private float fragmentColor[] = {00.2f, 0.709803922f, 0.898039216f, 1.0f };

    private int mVertexShader;
    private int mFragmentShader;

    private int mHorizontalProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTexCoordHandle;

    private int mRadiusHandle;
    private int mWidthOffsetHandle;
    private int mHeightOffsetHandle;

    private int vertexStride = COORDS_PER_VERTEX * 4;

    private Bitmap mBitmap;

    private ITexture mHorizontalTexture;
    private ITexture mVerticalTexture;
    private ITexture mInputTexture;
    private int mTextureUniformHandle;

    private IFrameBuffer mHorizontalFrameBuffer;

    private int mWidth;
    private int mHeight;

    private int mRadius;

    private TextureCache mTextureCache = TextureCache.getInstance();
    private FrameBufferCache mFrameBufferCache = FrameBufferCache.getInstance();

    public OffScreenRectangle(int blurRadius, @Blur.BlurMode int mode) {
        mRadius = blurRadius;

        fragmentShaderCode = ShaderUtil.getFragmentShaderCode(mode);

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
        mInputTexture = TextureFactory.create(mBitmap);

        initHorizontal();

    }

    private void initHorizontal() {
        mHorizontalProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mHorizontalProgram, mVertexShader);
        GLES20.glAttachShader(mHorizontalProgram, mFragmentShader);
        GLES20.glLinkProgram(mHorizontalProgram);


        mHorizontalTexture = mTextureCache.getTexture(mWidth, mHeight);
        mHorizontalFrameBuffer = mFrameBufferCache.getFrameBuffer();
        mHorizontalFrameBuffer.bindTexture(mHorizontalTexture);

    }


    public void draw(float[] mvpMatrix) {
        drawHorizontalBlur(mvpMatrix, true);
        drawHorizontalBlur(mvpMatrix, false);

    }

    private void drawHorizontalBlur(float[] mvpMatrix, boolean isHorizontal) {
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

        if (isHorizontal) {
            mHorizontalFrameBuffer.bindSelf();
        }

        mTextureUniformHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? mInputTexture.getId() : mHorizontalTexture.getId());
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mRadiusHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uRadius");
        mWidthOffsetHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uWidthOffset");
        mHeightOffsetHandle = GLES20.glGetUniformLocation(mHorizontalProgram, "uHeightOffset");
        GLES20.glUniform1i(mRadiusHandle, mRadius);
        GLES20.glUniform1f(mWidthOffsetHandle, isHorizontal ? 0 : 1f / mWidth);
        GLES20.glUniform1f(mHeightOffsetHandle, isHorizontal ? 1f / mHeight : 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        if (!isHorizontal) {
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mTexCoordHandle);
        }
        resetAllBuffer();

    }

    private void resetAllBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

        mVertexBuffer.rewind();
        mTexCoordBuffer.rewind();
        mDrawListBuffer.rewind();
    }
}
