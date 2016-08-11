package com.example.xiangpi.dynamicblurdemo.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.xiangpi.dynamicblurdemo.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by xiangpi on 16/8/10.
 */
public class Rectangle {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;   \n" +
            "attribute vec2 aTexCoord;   \n" +
            "attribute vec4 aPosition;  \n" +
            "varying vec2 vTexCoord;  \n" +
            "void main() {              \n" +
            "  gl_Position = uMVPMatrix * aPosition; \n" +
            "  vTexCoord = aTexCoord; \n" +
            "}  \n";

    private final String fragmentShaderCode =
                    "precision mediump float;   \n" +
                    "uniform vec4 vColor;   \n" +
                    "varying vec2 vTexCoord;   \n" +
                    "uniform sampler2D uTexture;   \n" +
                    "void main() {   \n" +
                    "  gl_FragColor = texture2D(uTexture, 1.0f - vTexCoord);   \n" +
//                    "  gl_FragColor = vColor;   \n" +
                    "}   \n";

    private FloatBuffer mVertexBuffer;

    private ShortBuffer mDrawListBuffer;

    private FloatBuffer mTexCoordBuffer;

    private static final int COORDS_PER_VERTEX = 3;

//    private float squareCoords[] = {
//                -0.5f,  0.5f, 0.0f,   // top left
//                -0.5f, -0.5f, 0.0f,   // bottom left
//                0.5f, -0.5f, 0.0f,   // bottom right
//                0.5f,  0.5f, 0.0f }; // top right

    private float squareCoords[];

    private static float texCoords[] = {
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f};

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private float fragmentColor[] = {00.2f, 0.709803922f, 0.898039216f, 1.0f };

    private int mVertexShader;
    private int mFragmentShader;

    private int mProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTexCoordHandle;

    private int vertexStride = COORDS_PER_VERTEX * 4;

    private Bitmap mBitmap;

    private int mTextureDataHandle;
    private int mTextureUniformHandle;

    private Context mCtx;

    private int mWidth;
    private int mHeight;

    public Rectangle(Context context) {

        mCtx = context;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        mBitmap = BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.sample1, options);
        if (mBitmap != null) {
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
        }
        setSquareCoords();

        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, mVertexShader);
        GLES20.glAttachShader(mProgram, mFragmentShader);

        GLES20.glLinkProgram(mProgram);

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

        ByteBuffer tcb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tcb.order(ByteOrder.nativeOrder());
        mTexCoordBuffer = tcb.asFloatBuffer();
        mTexCoordBuffer.put(texCoords);
        mTexCoordBuffer.position(0);

        mTextureDataHandle = loadTexture(mBitmap);

    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private int loadTexture(Bitmap bitmap) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureHandle[0];

    }

    private void setSquareCoords() {
        if (mWidth < mHeight) {
            final float ratio = (float) mWidth / mHeight;
            squareCoords = new float[]{
                -ratio / 2,  0.5f, 0.0f,   // top left
                -ratio / 2, -0.5f, 0.0f,   // bottom left
                ratio / 2, -0.5f, 0.0f,   // bottom right
                ratio / 2,  0.5f, 0.0f // top right
            };
        } else {
            final float ratio = (float) mHeight / mWidth;
            squareCoords = new float[] {
                -0.5f, ratio / 2, 0.0f,
                -0.5f, -ratio / 2, 0.0f,
                0.5f, -ratio / 2, 0.0f,
                0.5f, ratio / 2, 0.0f
            };
        }

    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, fragmentColor, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);

    }
}
