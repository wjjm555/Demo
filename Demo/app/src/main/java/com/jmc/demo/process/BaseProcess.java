package com.jmc.demo.process;

import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import com.jmc.demo.interfaces.ProcessSeniorListener;


public abstract class BaseProcess {

    private ProcessSeniorListener listener;

    private Context context;

    protected int processId;

    public abstract int getProcessId();

    public abstract void onFinish(boolean success);

    public abstract int getState();

    public BaseProcess() {
        this.processId = getProcessId();
    }

    public long getTimeOut() {
        return -1;
    }

    public Class<? extends BaseProcess> getNextProcess() {
        return null;
    }

    public Class<? extends BaseProcess> getCancelProcess() {
        return null;
    }

    @CallSuper
    public void executeProcess() {
        if (listener != null)
            listener.onProcessExecuted(getState(), processId);
    }

    @CallSuper
    public synchronized void cancelProcess() {
        if (listener != null) {
            listener.onProcessFinish(getState(), processId, false);
            listener.onCancelProcess(processId, getCancelProcess());
            onFinish(false);
        }
        setListener(null);
        setContext(null);
    }

    public void finishProcess(boolean success, Object... objects) {
        if (listener != null) {
            listener.onProcessFinish(getState(), processId, success, objects);
            listener.onNextProcess(processId, success ? getNextProcess() : null);
            onFinish(success);
        }
        setListener(null);
        setContext(null);
    }

    public void updateProgress(Object... objects) {
        if (listener != null)
            listener.onProcessUpdateProgress(getState(), processId, objects);
    }

    protected Context getContext() {
        return context;
    }



    public final void setListener(ProcessSeniorListener listener) {
        this.listener = listener;
    }

    public final void setContext(Context context) {
        this.context = context;
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        if (obj instanceof BaseProcess)
            return ((BaseProcess) obj).processId == processId;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "BaseProcess{" +
                "processId=" + processId +
                '}';
    }
}
