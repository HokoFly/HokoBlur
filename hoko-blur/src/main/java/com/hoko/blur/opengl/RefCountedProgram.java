package com.hoko.blur.opengl;

/**
 * Created by yuxfzju on 2025/7/10
 */
class RefCountedProgram {
    private final Program program;
    private int refCount = 1;
    private volatile boolean deleted = false;

    RefCountedProgram(String vertexShaderCode, String fragmentShaderCode) {
        program = Program.of(vertexShaderCode, fragmentShaderCode);
    }

    Program getProgram() {
        return program;
    }

    boolean incrementRefCount() {
        synchronized (this) {
            if (isInvalid()) {
                return false;
            }
            refCount++;
        }
        return true;
    }

    void decrementRefCount() {
        synchronized (this) {
            refCount--;
            if (refCount <= 0) {
                deleted = true;
                program.delete();
            }
        }
    }

    void clearRefCount() {
        synchronized (this) {
            refCount = 0;
            deleted = true;
            program.delete();
        }
    }

    int getRefCount() {
        return refCount;
    }

    boolean isInvalid() {
        return deleted || program.id() == 0;
    }
}
