package com.hoko.blur.api;

import com.hoko.blur.task.BlurResult;

public interface IBlurResultDispatcher {
    void postResult(BlurResult result);
}
