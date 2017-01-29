/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.utils;

/**
 *
 * @author matta
 */
public class StreamGobblerWrapper extends StreamGobbler {
    
    private StreamGobbler wrapped;

    public StreamGobblerWrapper(StreamGobbler wrapped) {
        super(null);
        this.wrapped = wrapped;
    }

    @Override
    public void run() {
    }

    @Override
    public String getUpdateText() {
        return wrapped == null ? "" : wrapped.getUpdateText();
    }
    
    public void setWrapped(StreamGobbler wrapped) {
        this.wrapped = wrapped;
    }
}
