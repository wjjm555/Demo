package com.jmc.demo.process;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;


import com.jmc.demo.interfaces.ProcessListener;
import com.jmc.demo.interfaces.ProcessSeniorListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProcessManager implements ProcessSeniorListener {

    final String TAG = ProcessManager.class.getSimpleName() ;

    private List<ProcessListener> callBacks = new CopyOnWriteArrayList<>();

    private BaseProcess currentProcess;

    private Context context;

    private Timer timer;

    private Handler mHandler;

    private static class InnerHolder {
        private static final ProcessManager INSTANCE = new ProcessManager();
    }

    public static ProcessManager getInstance() {
        return InnerHolder.INSTANCE;
    }

    private ProcessManager() {
        timer = new Timer();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void init(Context context) {
        this.context = context;
    }

    public void addCallBack(ProcessListener callBack) {
        callBacks.add(callBack);
    }

    public void removeCallBack(ProcessListener callBack) {
        callBacks.remove(callBack);
    }

    public void executeProcess(Class<? extends BaseProcess> processClass, Object... objects) {
        if (processClass != null) {
            try {
                BaseProcess process;
                if (objects == null || objects.length == 0)
                    process = processClass.newInstance();
                else {
                    Class[] classes = new Class[objects.length];
                    for (int i = 0; i < classes.length; ++i) {
                        if (objects[i] != null)
                            classes[i] = objects[i].getClass();
                    }
                    Constructor constructor = processClass.getConstructor(classes);
                    process = (BaseProcess) constructor.newInstance(objects);
                }
                executeProcess(process);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeProcess(BaseProcess process) {
        if (currentProcess == null) {
            currentProcess = process;
            new Thread(new MyProcessRunnable()).start();
        }
    }

    public void cancelProcess(int... processIds) {
        if (processIds.length > 0) {
            for (int processId : processIds) {
                if (currentProcess != null && currentProcess.getProcessId() == processId) {
                    currentProcess.cancelProcess();
                    break;
                }
            }
        } else {
            if (currentProcess != null) {
                currentProcess.cancelProcess();
            }
        }
    }

    public void finishProcess(int processId, Object... objects) {
        if (currentProcess != null && currentProcess.getProcessId() == processId) {
            currentProcess.finishProcess(true, objects);
        }
    }

    public boolean currentProcessRunning(int... processIds) {
        if (currentProcess == null) {
            return false;
        } else {
            if (processIds.length > 0) {
                for (int processId : processIds) {
                    if (currentProcess.getProcessId() == processId) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private void cleanProcess(int processId) {
        if (currentProcess != null && currentProcess.getProcessId() == processId) {
            currentProcess = null;
        }
    }

    @Override
    public void onProcessExecuted(int state, int processId, Object... objects) {
        mHandler.post(() -> {
            for (ProcessListener listener : callBacks)
                if (listener != null) listener.onProcessExecuted(state, processId, objects);
        });
    }

    @Override
    public void onProcessUpdateProgress(int state, int processId, Object... objects) {
        mHandler.post(() -> {
            for (ProcessListener listener : callBacks)
                if (listener != null) listener.onProcessUpdateProgress(state, processId, objects);
        });
    }

    @Override
    public void onProcessFinish(int state, int processId, boolean success, Object... objects) {
        cleanProcess(processId);
        mHandler.post(() -> {
            for (ProcessListener listener : callBacks)
                if (listener != null) listener.onProcessFinish(state, processId, success, objects);
        });
    }

    @Override
    public void onNextProcess(int lastProcessId, Class<? extends BaseProcess> process) {
        if (process != null) executeProcess(process);
    }

    @Override
    public void onCancelProcess(int lastProcessId, Class<? extends BaseProcess> process) {
        if (process != null) executeProcess(process);
    }

    class MyProcessRunnable implements Runnable {

        @Override
        public void run() {
            if (currentProcess != null) {
                currentProcess.setContext(context);
                currentProcess.setListener(ProcessManager.this);
                if (currentProcess.getTimeOut() > 0)
                    timer.schedule(new MyTimerTask(currentProcess), currentProcess.getTimeOut());
                currentProcess.executeProcess();
            }
        }
    }

    class MyTimerTask extends TimerTask {

        BaseProcess currentProcess;

        MyTimerTask(BaseProcess currentProcess) {
            this.currentProcess = currentProcess;
        }

        @Override
        public void run() {
            if (currentProcess != null) currentProcess.cancelProcess();
        }
    }

}
