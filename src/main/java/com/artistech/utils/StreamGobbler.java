/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for accessing the output stream of a process.
 *
 * @author matta
 */
public class StreamGobbler extends Thread {

    private final Mailbox<String> mailbox;
    private final InputStream is;

    /**
     * Constructor.
     *
     * @param is
     */
    public StreamGobbler(InputStream is) {
        mailbox = new Mailbox<>();
        this.is = is;
    }

    /**
     * Threaded gobbler.
     */
    @Override
    public void run() {
        Logger logger = Logger.getLogger(StreamGobbler.class.getName());
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                mailbox.addMessage(line);
                logger.log(Level.WARNING, line);
            }
            mailbox.halt();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }
    }

    /**
     * Get the latest text.
     *
     * @return
     */
    public String getUpdateText() {
        ArrayList<String> messages = mailbox.getMessages();
        StringBuilder sb = new StringBuilder();
        if (messages != null) {
            for (String line : messages) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString().replaceAll("\\\\r", "").replaceAll("\\\\n", "\n");
    }
}
