/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.web;

import com.artistech.ee.beans.DataManager;
import com.artistech.ee.beans.DataBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 * View an output file. Determine output mime-type from extension.
 *
 * @author matta
 */
@WebServlet(name = "ViewRaw", urlPatterns = {"/ViewRaw"})
public class ViewRaw extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pipeline_id = request.getParameter("pipeline_id");
        String stage = request.getParameter("stage");
        String file = request.getParameter("file");
        DataBase data = DataManager.getData(pipeline_id);
        File ftest = new File(file);
        if (file.endsWith(".xml")) {
            response.setContentType("text/xml;charset=UTF-8");
        } else if (file.endsWith(".html")) {
            response.setContentType("text/html;charset=UTF-8");
        } else if (file.endsWith(".json")) {
            response.setContentType("text/json;charset=UTF-8");
        } else if (file.endsWith(".html")) {
            response.setContentType("text/plain;charset=UTF-8");
        }
        if (ftest.exists()) {
            IOUtils.copy(new FileInputStream(ftest), response.getWriter(), "UTF-8");
        } else {
            String fileName = data.getData(stage) + File.separator + file;
            File f = new File(fileName);
            IOUtils.copy(new FileInputStream(f), response.getWriter(), "UTF-8");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "View output file";
    }// </editor-fold>

}
