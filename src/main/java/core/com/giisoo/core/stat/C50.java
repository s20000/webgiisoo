package com.giisoo.core.stat;

import com.giisoo.core.worker.WorkerTask;

public class C50 extends WorkerTask {

    public boolean done = false;

    @Override
    public void onExecute() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFinish() {
        done = true;
    }

}
