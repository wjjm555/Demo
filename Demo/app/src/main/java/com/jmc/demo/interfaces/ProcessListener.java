package com.jmc.demo.interfaces;

public interface ProcessListener {
    void onProcessExecuted(int state, int processId, Object... objects);

    void onProcessUpdateProgress(int state, int processId, Object... objects);

    void onProcessFinish(int state, int processId, boolean success, Object... objects);
}
