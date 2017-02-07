/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.event.EventListenerSupport;

/**
 * Wrapper for accessing the output stream of a process.
 *
 * @author matta
 */
public class StreamGobbler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(StreamGobbler.class.getName());

    protected final OutputStream os;
    protected final EventListenerSupport<GobblerListener> listeners
            = EventListenerSupport.create(GobblerListener.class);

    private final Mailbox<String> mailbox;
    private final InputStream is;
    private final Writer writer;

    /**
     * Constructor.
     *
     * @param is
     * @param os
     */
    public StreamGobbler(InputStream is, OutputStream os) {
        mailbox = new Mailbox<>();
        this.is = is;
        this.os = os;
        if (this.os == null) {
            writer = null;
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(this.os));
        }

        addGobblerListener(new GobblerListener() {
            @Override
            public void write(String line) {
                mailbox.addMessage(line);
            }

            @Override
            public void complete() {
            }

            @Override
            public void start() {
            }
        });
        addGobblerListener(new GobblerListener() {
            @Override
            public void write(String line) {
                LOGGER.log(Level.WARNING, line);
            }

            @Override
            public void complete() {
            }

            @Override
            public void start() {
            }
        });
        if (this.writer != null) {
            addGobblerListener(new GobblerListener() {
                @Override
                public void write(String line) {
                    try {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void complete() {
                    try {
                        writer.write(System.lineSeparator());
                        writer.flush();
                        writer.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void start() {
                }
            });
        }
    }

    public final void addGobblerListener(GobblerListener listener) {
        listeners.addListener(listener);
    }

    public final void removeGobblerListener(GobblerListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Constructor.
     *
     * @param is
     */
    public StreamGobbler(InputStream is) {
        this(is, null);
    }

    /**
     * Threaded gobbler.
     */
    @Override
    public void run() {
        listeners.fire().start();
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                write(line);
            }
            mailbox.halt();
        } catch (IOException ioe) {
//            LOGGER.log(Level.SEVERE, null, ioe);
        } finally {
            listeners.fire().complete();
        }
    }

    public void write(String line) {
        listeners.fire().write(line);
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
