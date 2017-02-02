/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.utils;

/**
 * Container for an external process or thread that is running for the
 * web-application to monitor.
 *
 * @author matta
 */
public class ExternalProcess {

    private final StreamGobbler sg;
    private final Process proc;
    private final Thread thread;

    /**
     * Constructor with a process.
     *
     * @param sg
     * @param proc
     */
    public ExternalProcess(StreamGobbler sg, Process proc) {
        this.sg = sg;
        this.proc = proc;
        this.thread = null;
    }

    /**
     * Constructor with a thread.
     *
     * @param sg
     * @param thread
     */
    public ExternalProcess(StreamGobbler sg, Thread thread) {
        this.sg = sg;
        this.proc = null;
        this.thread = thread;
    }

    /**
     * Kill the current thread or process.
     */
    public void kill() {
        if (proc != null) {
            proc.destroy();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Is the current thread or process alive.
     *
     * @return
     */
    public boolean isAlive() {
        if (proc != null) {
            return proc.isAlive();
        }
        if (thread != null) {
            return thread.isAlive();
        }
        return false;
    }

    /**
     * Get the stream gobbler for accessing the output stream.
     *
     * @return
     */
    public StreamGobbler getGobbler() {
        return sg;
    }
}
