package com.hoko.blur.opengl.offscreen;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.hoko.blur.anno.Mode;
import com.hoko.blur.api.IFrameBuffer;
import com.hoko.blur.api.IRenderer;
import com.hoko.blur.opengl.cache.FrameBufferCache;
import com.hoko.blur.opengl.texture.Texture;
import com.hoko.blur.opengl.texture.TextureFactory;
import com.hoko.blur.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import static com.hoko.blur.util.ShaderUtil.checkGLError;

/**
 * Created by yuxfzju on 16/8/10.
 */
public class OffScreenBlurRenderer implements IRenderer<Bitmap> {
    private final static String TAG = OffScreenBlurRenderer.class.getSimpleName();

    private static final String vertexShaderCode =
            "attribute vec2 aTexCoord;   \n" +
                    "attribute vec4 aPosition;  \n" +
                    "varying vec2 vTexCoord;  \n" +
                    "void main() {              \n" +
                    "  gl_Position = aPosition; \n" +
                    "  vTexCoord = aTexCoord; \n" +
                    "}  \n";

    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private static final float squareCoords[] = {
            -1f, 1f, 0.0f,   // top left
            -1f, -1f, 0.0f,   // bottom left
            1f, -1f, 0.0f,   // bottom right
            1f, 1f, 0.0f}; // top right

    private static final float mTexHorizontalCoords[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f};

    private static final short drawOrder[] = {0, 1, 2, 0, 2, 3};

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mTexCoordBuffer;

    private int mProgram;

    private Texture mHorizontalTexture;
    private Texture mInputTexture;

    private IFrameBuffer mBlurFrameBuffer;

    private Bitmap mBitmap;

    private int mWidth;
    private int mHeight;

    private int mRadius;
    @Mode
    private int mMode;

    private volatile boolean mNeedRelink;

    public OffScreenBlurRenderer() {

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
    public void onDrawFrame(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        mBitmap = bitmap;
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();

        if (mWidth == 0 || mHeight == 0) {
            return;
        }

        try {
            if (prepare()) {
                draw();
            }
        } finally {
            onPostBlur();
        }
    }

    @Override
    public void onSurfaceCreated() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    private boolean prepare() {
        EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        if (context.equals(EGL10.EGL_NO_CONTEXT)) {
            Log.e(TAG, "This thread has no EGLContext.");
            return false;
        }

        if (mNeedRelink) {
            deletePrograms();
            mProgram = ShaderUtil.createProgram(vertexShaderCode, ShaderUtil.getFragmentShaderCode(mMode));
            mNeedRelink = false;
        }

        if (mProgram == 0) {
            Log.e(TAG, "Failed to create program.");
            return false;
        }

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        //todo Textures share problem is not solved. Here create a new texture directly, not get from the texture cache
        //It doesn't affect performance seriously.
        mInputTexture = TextureFactory.create(mBitmap);
        mHorizontalTexture = TextureFactory.create(mWidth, mHeight);
        mBlurFrameBuffer = FrameBufferCache.getInstance().getFrameBuffer();
        if (mBlurFrameBuffer != null) {
            mBlurFrameBuffer.bindTexture(mHorizontalTexture);
        } else {
            Log.e(TAG, "Failed to create framebuffer.");
            return false;
        }

        return checkGLError("Prepare to blurring");

    }


    private void draw() {
        drawOneDimenBlur(true);
        drawOneDimenBlur(false);

    }

    private void drawOneDimenBlur(boolean isHorizontal) {
        try {
            GLES20.glUseProgram(mProgram);

            int positionId = GLES20.glGetAttribLocation(mProgram, "aPosition");
            GLES20.glEnableVertexAttribArray(positionId);
            GLES20.glVertexAttribPointer(positionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

            int texCoordId = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
            GLES20.glEnableVertexAttribArray(texCoordId);
            GLES20.glVertexAttribPointer(texCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

            if (isHorizontal) {
                mBlurFrameBuffer.bindSelf();
            }

            int textureUniformId = GLES20.glGetUniformLocation(mProgram, "uTexture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? mInputTexture.id() : mHorizontalTexture.id());
            GLES20.glUniform1i(textureUniformId, 0);

            int radiusId = GLES20.glGetUniformLocation(mProgram, "uRadius");
            int widthOffsetId = GLES20.glGetUniformLocation(mProgram, "uWidthOffset");
            int heightOffsetId = GLES20.glGetUniformLocation(mProgram, "uHeightOffset");
            GLES20.glUniform1i(radiusId, mRadius);
            GLES20.glUniform1f(widthOffsetId, isHorizontal ? 0 : 1f / mWidth);
            GLES20.glUniform1f(heightOffsetId, isHorizontal ? 1f / mHeight : 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

            if (!isHorizontal) {
                GLES20.glDisableVertexAttribArray(positionId);
                GLES20.glDisableVertexAttribArray(texCoordId);
            }
        } finally {
            resetAllBuffer();
        }

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
        if (mInputTexture != null) {
            mInputTexture.delete();
        }
        if (mHorizontalTexture != null) {
            mHorizontalTexture.delete();
        }
        FrameBufferCache.getInstance().recycleFrameBuffer(mBlurFrameBuffer);
    }


    private void deletePrograms() {
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
        }
    }

    @Override
    public void free() {
        mNeedRelink = true;
        deletePrograms();
    }

    public void setBlurMode(@Mode int mode) {
        mNeedRelink = true;
        mMode = mode;
    }

    public void setBlurRadius(int radius) {
        mRadius = radius;
    }

}
