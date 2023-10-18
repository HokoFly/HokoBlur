package com.hoko.blur.opengl;

import android.opengl.GLES20;
import android.util.Log;

import static com.hoko.blur.util.ShaderUtil.checkGLError;

class Program {

    private static final String TAG = Program.class.getSimpleName();

    private int id;

    public static Program of(String vertexShaderCode, String fragmentShaderCode) {
        return new Program(vertexShaderCode, fragmentShaderCode);
    }

    private Program(String vertexShaderCode, String fragmentShaderCode) {
        create(vertexShaderCode, fragmentShaderCode);
    }

    public void create(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = 0;
        int fragmentShader = 0;
        try {
            vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
            if (vertexShader == 0 || fragmentShader == 0) {
                return;
            }
            id = GLES20.glCreateProgram();
            if (id != 0) {
                GLES20.glAttachShader(id, vertexShader);
                GLES20.glAttachShader(id, fragmentShader);
                GLES20.glLinkProgram(id);
                checkGLError("Attach Shader");
                final int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(id, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] != 1) {
                    Log.e(TAG, "Failed to link program");
                    GLES20.glDeleteProgram(id);
                    id = 0;
                }
            }
        } finally {
            GLES20.glDetachShader(id, vertexShader);
            GLES20.glDetachShader(id, fragmentShader);
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragmentShader);
        }
    }

    public void delete() {
        if (id != 0) {
            GLES20.glUseProgram(0);
            GLES20.glDeleteProgram(id);
        }
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        if (shader != 0) {
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            final int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Failed to compile the shader");
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public int id() {
        return id;
    }
}
