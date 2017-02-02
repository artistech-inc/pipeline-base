/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.web;

import com.artistech.ee.beans.DataManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Set the data_path value globally.
 *
 * @author matta
 */
@WebListener
public class PipelineInit implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(PipelineInit.class.getName());

    /**
     * Get the port value and then set the static QOI configuration port value.
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.FINER, "Context Initialized!");
        String initParameter = sce.getServletContext().getInitParameter("data_path");
        DataManager.setDataPath(initParameter);
    }

    /**
     * Destroyed Event.
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.FINER, "Context Destroyed!");
    }

}
