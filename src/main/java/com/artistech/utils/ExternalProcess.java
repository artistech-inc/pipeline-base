/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.utils;

/**
 *
 * @author matta
 */
public class ExternalProcess {

    private final StreamGobbler sg;
    private final Process proc;
    private final Thread thread;

    public ExternalProcess(StreamGobbler sg, Process proc) {
        this.sg = sg;
        this.proc = proc;
        this.thread = null;
    }

    public ExternalProcess(StreamGobbler sg, Thread thread) {
        this.sg = sg;
        this.proc = null;
        this.thread = thread;
    }

    public void kill() {
        if (proc != null) {
            proc.destroy();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    public boolean isAlive() {
        if (proc != null) {
            return proc.isAlive();
        }
        if (thread != null) {
            return thread.isAlive();
        }
        return false;
    }

    public StreamGobbler getGobbler() {
        return sg;
    }
}
