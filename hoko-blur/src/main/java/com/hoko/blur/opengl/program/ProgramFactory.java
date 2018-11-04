package com.hoko.blur.opengl.program;

import com.hoko.blur.api.IProgram;

public class ProgramFactory {

    public static IProgram create(String vertexShaderCode, String fragmentShaderCode) {
        return new Program(vertexShaderCode, fragmentShaderCode);
    }
}
