/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.utils;

/**
 * Allows wrapping a gobbler. This is useful for working with threaded work
 * instead of external processes.
 *
 * @author matta
 */
public class StreamGobblerWrapper extends StreamGobbler {

    private StreamGobbler wrapped;

    /**
     * Constructor.
     *
     * @param wrapped
     */
    public StreamGobblerWrapper(StreamGobbler wrapped) {
        super(null);
        this.wrapped = wrapped;
    }

    /**
     * Empty run.
     */
    @Override
    public void run() {
    }

    /**
     * Get the text.
     *
     * @return
     */
    @Override
    public String getUpdateText() {
        return wrapped == null ? "" : wrapped.getUpdateText();
    }

    /**
     * Set a new wrapped object.
     *
     * @param wrapped
     */
    public void setWrapped(StreamGobbler wrapped) {
        this.wrapped = wrapped;
    }
}
