package com.jmc.demo.interfaces;


import com.jmc.demo.process.BaseProcess;

public interface ProcessSeniorListener extends ProcessListener {
    void onNextProcess(int lastProcessId, Class<? extends BaseProcess> process);

    void onCancelProcess(int lastProcessId, Class<? extends BaseProcess> process);
}
