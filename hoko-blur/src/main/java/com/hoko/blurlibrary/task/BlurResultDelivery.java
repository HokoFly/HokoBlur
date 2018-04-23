package com.hoko.blurlibrary.task;

import java.util.concurrent.Executor;

/**
 * 结果的分发，指定最后回调的线程
 * Created by yuxfzju on 2017/2/7.
 */

public class BlurResultDelivery {

    private Executor mResultPoster;

    public BlurResultDelivery(final android.os.Handler handler) {
        mResultPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                if (handler != null) {
                    handler.post(command);
                }
            }
        };
    }

    public void postResult(BlurResult result) {
        mResultPoster.execute(new BlurResultDeliveryRunnable(result));
    }
}
