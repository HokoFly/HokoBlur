package com.hoko.blurlibrary.api;

import com.hoko.blurlibrary.task.BlurResult;

public interface IBlurResultDispatcher {
    void postResult(BlurResult result);
}
