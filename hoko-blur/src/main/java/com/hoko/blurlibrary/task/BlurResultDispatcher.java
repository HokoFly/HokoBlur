package com.hoko.blurlibrary.task;

import com.hoko.blurlibrary.api.IBlurResultDispatcher;

import java.util.concurrent.Executor;

/**
 * 结果的分发，指定最后回调的线程
 * Created by yuxfzju on 2017/2/7.
 */

public class BlurResultDispatcher implements IBlurResultDispatcher {

    private Executor mResultPoster;

    public BlurResultDispatcher(final android.os.Handler handler) {
        mResultPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                if (handler != null) {
                    handler.post(command);
                }
            }
        };
    }

    @Override
    public void postResult(BlurResult result) {
        mResultPoster.execute(new BlurResultDeliveryRunnable(result));
    }
}
