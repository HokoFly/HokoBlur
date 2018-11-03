package com.hoko.blur.api;

import com.hoko.blur.task.BlurResult;

public interface IBlurResultDispatcher {
    void dispatch(BlurResult result);
}
