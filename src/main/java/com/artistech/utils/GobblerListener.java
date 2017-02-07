/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.utils;

/**
 *
 * @author matta
 */
public interface GobblerListener {

    void write(String line);

    void complete();

    void start();
}
