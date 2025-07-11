package com.hoko.blur.opengl;

import com.hoko.blur.anno.Mode;
import com.hoko.blur.util.ShaderUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yuxfzju on 2025/7/10
 */
class ProgramManager {
    private final Map<Integer, RefCountedProgram> programCache = new ConcurrentHashMap<>();

    private static class ProgramManagerHolder {
        private static final ProgramManager INSTANCE = new ProgramManager();
    }

    public static ProgramManager getInstance() {
        return ProgramManagerHolder.INSTANCE;
    }


    private ProgramManager() {

    }

    Program getProgram(@Mode int mode) {
        synchronized (this) {
            RefCountedProgram refCountedProgram = programCache.get(mode);
            if (refCountedProgram != null) {
                if (refCountedProgram.incrementRefCount()) {
                    return refCountedProgram.getProgram();
                } else {
                    // invalid program
                    refCountedProgram.clearRefCount();
                    programCache.remove(mode);
                }
            }

            refCountedProgram = new RefCountedProgram(ShaderUtil.getVertexCode(), ShaderUtil.getFragmentShaderCode(mode));
            programCache.put(mode, refCountedProgram);
            return refCountedProgram.getProgram();
        }
    }

    void releaseProgram(Program program) {
        if (program == null) {
            return;
        }
        synchronized (this) {
            Iterator<Map.Entry<Integer, RefCountedProgram>> it = programCache.entrySet().iterator();
            while (it.hasNext()) {
                RefCountedProgram refCountedProgram = it.next().getValue();
                if (refCountedProgram.getProgram() == program) {
                    refCountedProgram.decrementRefCount();
                    if (refCountedProgram.getRefCount() <= 0) {
                        it.remove();
                        return;
                    }
                }
            }
        }
        program.delete();
    }



}
