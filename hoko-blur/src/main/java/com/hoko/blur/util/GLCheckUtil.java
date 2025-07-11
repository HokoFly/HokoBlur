package com.hoko.blur.util;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by yuxfzju on 2025/7/10
 */
class GLCheckUtil {
    private static final String TAG = "GLCheckUtil";

    private void checkGlState(String stage) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "OpenGL error at " + stage + ": " + getGLErrorString(error));
        }
    }

    private String getGLErrorString(int error) {
        switch (error) {
            case GLES20.GL_INVALID_ENUM: return "GL_INVALID_ENUM";
            case GLES20.GL_INVALID_VALUE: return "GL_INVALID_VALUE";
            case GLES20.GL_INVALID_OPERATION: return "GL_INVALID_OPERATION";
            case GLES20.GL_OUT_OF_MEMORY: return "GL_OUT_OF_MEMORY";
            default: return "Unknown error: " + error;
        }
    }

    private void checkFramebufferStatus() {
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            String statusStr;
            switch (status) {
                case GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                    statusStr = "Incomplete attachment"; break;
                case GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                    statusStr = "Incomplete dimensions"; break;
                case GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                    statusStr = "Missing attachment"; break;
                case GLES20.GL_FRAMEBUFFER_UNSUPPORTED:
                    statusStr = "Unsupported"; break;
                default: statusStr = "Unknown: " + status;
            }
            throw new IllegalStateException("Framebuffer not complete: " + statusStr);
        }
    }

}
