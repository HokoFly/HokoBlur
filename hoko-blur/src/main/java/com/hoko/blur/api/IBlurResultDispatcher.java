package com.hoko.blur.api;

import com.hoko.blur.task.BlurResultRunnable;

public interface IBlurResultDispatcher {
    void dispatch(BlurResultRunnable result);
}
