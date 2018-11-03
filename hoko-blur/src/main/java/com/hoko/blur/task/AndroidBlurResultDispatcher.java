package com.hoko.blur.task;

import com.hoko.blur.api.IBlurResultDispatcher;
import com.hoko.blur.util.SingleMainHandler;

import java.util.concurrent.Executor;

/**
 * 结果的分发，指定最后回调的线程
 * Created by yuxfzju on 2017/2/7.
 */

public class AndroidBlurResultDispatcher implements IBlurResultDispatcher {

    static final IBlurResultDispatcher MAIN_THREAD_DISPATCHER = new AndroidBlurResultDispatcher(SingleMainHandler.get());

    private Executor mResultPoster;

    public AndroidBlurResultDispatcher(final android.os.Handler handler) {
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
    public void dispatch(BlurResult result) {
        mResultPoster.execute(new BlurResultDeliveryRunnable(result));
    }
}
