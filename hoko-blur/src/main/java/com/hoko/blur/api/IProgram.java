package com.hoko.blur.api;

public interface IProgram {

    void create(String vertexShaderCode, String fragmentShaderCode);

    void delete();

    int id();
}
