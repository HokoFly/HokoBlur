package com.hoko.blur.task;

import com.hoko.blur.api.IBlurResultDispatcher;
import com.hoko.blur.util.SingleMainHandler;

import java.util.concurrent.Executor;

/**
 * Created by yuxfzju on 2017/2/7.
 */

public class AndroidBlurResultDispatcher implements IBlurResultDispatcher {

    public static final IBlurResultDispatcher MAIN_THREAD_DISPATCHER = new AndroidBlurResultDispatcher(SingleMainHandler.get());

    private Executor mResultPoster;

    public AndroidBlurResultDispatcher(final android.os.Handler handler) {
        mResultPoster = command -> {
            if (handler != null) {
                handler.post(command);
            }
        };
    }

    @Override
    public void dispatch(Runnable runnable) {
        mResultPoster.execute(runnable);
    }
}
