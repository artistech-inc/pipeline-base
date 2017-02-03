/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
