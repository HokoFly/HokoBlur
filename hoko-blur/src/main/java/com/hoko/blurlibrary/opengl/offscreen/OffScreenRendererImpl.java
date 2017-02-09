package com.hoko.blurlibrary.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.hoko.blurlibrary.Blur;
import com.hoko.blurlibrary.opengl.cache.FrameBufferCache;
import com.hoko.blurlibrary.api.IFrameBuffer;
import com.hoko.blurlibrary.api.ITexture;
import com.hoko.blurlibrary.opengl.texture.TextureFactory;
import com.hoko.blurlibrary.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import static com.hoko.blurlibrary.util.ShaderUtil.checkGLError;

/**
 * Created by xiangpi on 16/8/10.
 */
public class OffScreenRendererImpl implements GLRenderer{
    private final static String TAG = OffScreenRendererImpl.class.getSimpleName();

    private final String vertexShaderCode =
                    "attribute vec2 aTexCoord;   \n" +
                    "attribute vec4 aPosition;  \n" +
                    "varying vec2 vTexCoord;  \n" +
                    "void main() {              \n" +
                    "  gl_Position = aPosition; \n" +
                    "  vTexCoord = aTexCoord; \n" +
                    "}  \n";


    private FloatBuffer mVertexBuffer;

    private ShortBuffer mDrawListBuffer;

    private FloatBuffer mTexCoordBuffer;

    private static final int COORDS_PER_VERTEX = 3;

    private float squareCoords[] = {
            -1f, 1f, 0.0f,   // top left
            -1f, -1f, 0.0f,   // bottom left
            1f, -1f, 0.0f,   // bottom right
            1f, 1f, 0.0f}; // top right

    private static float mTexHorizontalCoords[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f};

    private short drawOrder[] = {0, 1, 2, 0, 2, 3};

    private float fragmentColor[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

    private int mProgram;

    private int mPositionId;
    private int mColorHandle;
    private int mTexCoordId;

    private int mRadiusId;
    private int mWidthOffsetId;
    private int mHeightOffsetId;

    private int vertexStride = COORDS_PER_VERTEX * 4;

    private Bitmap mBitmap;

    private ITexture mHorizontalTexture;
    private ITexture mInputTexture;
    private int mTextureUniformId;

    private IFrameBuffer mHorizontalFrameBuffer;

    private int mWidth;
    private int mHeight;

    private int mRadius;
    private @Blur.Mode
    int mMode;

    private FrameBufferCache mFrameBufferCache = FrameBufferCache.getInstance();
    private boolean mHasEGLContext;
    private boolean mNeedRelink;

    public OffScreenRendererImpl() {

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

    private void doBlur(Bitmap bitmap) {

        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        mBitmap = bitmap;
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();

        if (mWidth == 0 || mHeight == 0) {
            return;
        }

        if (prepare()) {
            draw();
        }

        onPostBlur();

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

        if (mNeedRelink) {
            deletePrograms();
            mProgram = ShaderUtil.createProgram(vertexShaderCode, ShaderUtil.getFragmentShaderCode(mMode));
            mNeedRelink = false;
        }

        if (mProgram == 0) {
            return false;
        }

        //未解决多线程下的共享纹理问题，这里不再使用缓存池，直接创建新的Texture
        //另外，影响性能的主要矛盾不再于此
        mInputTexture = TextureFactory.create(mBitmap);
        mHorizontalTexture = TextureFactory.create(mWidth, mHeight);
        mHorizontalFrameBuffer = mFrameBufferCache.getFrameBuffer();
        if (mHorizontalFrameBuffer != null) {
            mHorizontalFrameBuffer.bindTexture(mHorizontalTexture);
        }

        return checkGLError("Prepare to blurring");

    }


    private void draw() {
        drawOneDimenBlur(true);
        drawOneDimenBlur(false);

    }

    private void drawOneDimenBlur(boolean isHorizontal) {
        GLES20.glUseProgram(mProgram);

        mPositionId = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionId);
        GLES20.glVertexAttribPointer(mPositionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

        mTexCoordId = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordId);
        GLES20.glVertexAttribPointer(mTexCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        if (isHorizontal) {
            mHorizontalFrameBuffer.bindSelf();
        }

        mTextureUniformId = GLES20.glGetUniformLocation(mProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? mInputTexture.getId() : mHorizontalTexture.getId());
        GLES20.glUniform1i(mTextureUniformId, 0);

        mRadiusId = GLES20.glGetUniformLocation(mProgram, "uRadius");
        mWidthOffsetId = GLES20.glGetUniformLocation(mProgram, "uWidthOffset");
        mHeightOffsetId = GLES20.glGetUniformLocation(mProgram, "uHeightOffset");
        GLES20.glUniform1i(mRadiusId, mRadius);
        GLES20.glUniform1f(mWidthOffsetId, isHorizontal ? 0 : 1f / mWidth);
        GLES20.glUniform1f(mHeightOffsetId, isHorizontal ? 1f / mHeight : 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        if (!isHorizontal) {
            GLES20.glDisableVertexAttribArray(mPositionId);
            GLES20.glDisableVertexAttribArray(mTexCoordId);
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

    private void onPostBlur() {
        mInputTexture.delete();
        mHorizontalTexture.delete();
        mFrameBufferCache.recycleFrameBuffer(mHorizontalFrameBuffer);
    }


    private void deletePrograms() {
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
        }
    }

    public void free() {
        mNeedRelink = true;
        deletePrograms();
    }


    public void setBlurMode(@Blur.Mode int mode) {
        mNeedRelink = true;
        mMode = mode;
    }

    public void setBlurRadius(int radius) {
        mRadius = radius;
    }

    @Override
    public void onDrawFrame(Bitmap bitmap) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        doBlur(bitmap);
    }

    @Override
    public void onSurfaceCreated() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

    }

}
