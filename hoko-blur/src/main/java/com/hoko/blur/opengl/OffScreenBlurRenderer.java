package com.hoko.blur.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.hoko.blur.anno.Mode;
import com.hoko.blur.anno.NotThreadSafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by yuxfzju on 16/8/10.
 */

@NotThreadSafe
class OffScreenBlurRenderer implements IRenderer<Bitmap> {
    private final static String TAG = OffScreenBlurRenderer.class.getSimpleName();

    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private static final float[] squareCoords = {
            -1f, 1f, 0.0f,   // top left
            -1f, -1f, 0.0f,   // bottom left
            1f, -1f, 0.0f,   // bottom right
            1f, 1f, 0.0f    // top right
    };

    private static final float[] mTexHorizontalCoords = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    private static final short[] drawOrder = {0, 1, 2, 0, 2, 3};

    private final FloatBuffer mVertexBuffer;
    private final ShortBuffer mDrawListBuffer;
    private final FloatBuffer mTexCoordBuffer;

    private Program mProgram;

    private int mRadius;
    @Mode
    private int mMode;

    public OffScreenBlurRenderer(@Mode int mode, int radius) {
        mMode = mode;
        mRadius = radius;

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

        mProgram = ProgramManager.getInstance().getProgram(mMode);
    }

    @Override
    public void onDrawFrame(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        if (bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            return;
        }
        BlurContext blurContext = null;
        try {
            blurContext = prepare(bitmap);
            draw(blurContext);
        } finally {
            onPostBlur(blurContext);
        }
    }


    private BlurContext prepare(Bitmap bitmap) {
        EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        if (context.equals(EGL10.EGL_NO_CONTEXT)) {
            throw new IllegalStateException("This thread has no EGLContext.");
        }
        if (mProgram == null || mProgram.id() == 0) {
            throw new IllegalStateException("Failed to create program.");
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, w, h);
        return new BlurContext(bitmap);
    }


    private void draw(BlurContext blurContext) {
        drawOneDimenBlur(blurContext, true);
        drawOneDimenBlur(blurContext, false);
        GLES20.glFinish();
    }

    private void drawOneDimenBlur(BlurContext blurContext, boolean isHorizontal) {
        try {
            GLES20.glUseProgram(mProgram.id());

            int positionId = GLES20.glGetAttribLocation(mProgram.id(), "aPosition");
            GLES20.glEnableVertexAttribArray(positionId);
            GLES20.glVertexAttribPointer(positionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

            int texCoordId = GLES20.glGetAttribLocation(mProgram.id(), "aTexCoord");
            GLES20.glEnableVertexAttribArray(texCoordId);
            GLES20.glVertexAttribPointer(texCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

            if (isHorizontal) {
                blurContext.getBlurFrameBuffer().bindSelf();
            }

            int textureUniformId = GLES20.glGetUniformLocation(mProgram.id(), "uTexture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? blurContext.getInputTexture().id() : blurContext.getHorizontalTexture().id());
            GLES20.glUniform1i(textureUniformId, 0);

            int radiusId = GLES20.glGetUniformLocation(mProgram.id(), "uRadius");
            int widthOffsetId = GLES20.glGetUniformLocation(mProgram.id(), "uWidthOffset");
            int heightOffsetId = GLES20.glGetUniformLocation(mProgram.id(), "uHeightOffset");
            GLES20.glUniform1i(radiusId, mRadius);
            GLES20.glUniform1f(widthOffsetId, isHorizontal ? 0 : 1f / blurContext.getBitmap().getWidth());
            GLES20.glUniform1f(heightOffsetId, isHorizontal ? 1f / blurContext.getBitmap().getHeight() : 0);

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

    private void onPostBlur(BlurContext blurContext) {
        if (blurContext != null) {
            blurContext.finish();
        }
        mVertexBuffer.clear();
        mTexCoordBuffer.clear();
        mDrawListBuffer.clear();
        releaseProgram();
    }


    private void releaseProgram() {
        if (mProgram != null) {
            ProgramManager.getInstance().releaseProgram(mProgram);
        }
    }

    private static class BlurContext {
        private final Texture inputTexture;
        private final Texture horizontalTexture;
        private final FrameBuffer blurFrameBuffer;
        private final Bitmap bitmap;

        private BlurContext(Bitmap bitmap) {
            this.bitmap = bitmap;
            inputTexture = TextureCache.getInstance().acquireTexture(bitmap.getWidth(), bitmap.getHeight());
            inputTexture.uploadBitmap(bitmap);
            horizontalTexture = TextureCache.getInstance().acquireTexture(bitmap.getWidth(), bitmap.getHeight());
            blurFrameBuffer = FrameBufferCache.getInstance().getFrameBuffer();
            if (blurFrameBuffer != null) {
                blurFrameBuffer.bindTexture(horizontalTexture);
            } else {
                throw new IllegalStateException("Failed to create framebuffer.");
            }
        }

        private Texture getInputTexture() {
            return inputTexture;
        }

        private Texture getHorizontalTexture() {
            return horizontalTexture;
        }

        private FrameBuffer getBlurFrameBuffer() {
            return blurFrameBuffer;
        }

        private Bitmap getBitmap() {
            return bitmap;
        }

        private void finish() {
            blurFrameBuffer.unbindTexture();
            if (inputTexture != null) {
                TextureCache.getInstance().releaseTexture(inputTexture);
            }
            if (horizontalTexture != null) {
                TextureCache.getInstance().releaseTexture(horizontalTexture);
            }
            FrameBufferCache.getInstance().recycleFrameBuffer(blurFrameBuffer);
        }
    }

}
