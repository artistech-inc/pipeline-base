package com.artistech.ee.web;

/*
 * Copyright 2017 ArtisTech, Inc.
 */
import com.artistech.ee.beans.DataManager;
import com.artistech.ee.beans.DataBase;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get if the current process is still running.
 *
 * @author matta
 */
@WebServlet(name = "ProcessMonitor", urlPatterns = {"/ProcessMonitor"})
public class ProcessMonitor extends HttpServlet {

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
        response.setContentType("text/plain;charset=UTF-8");
        String pipeline_id = request.getParameter("pipeline_id");
//        String pipeline_id = IOUtils.toString(pipeline_id_part.getInputStream(), "UTF-8");
        DataBase data = DataManager.getData(pipeline_id);

        try (PrintWriter out = response.getWriter()) {
            if (data.getProc() != null) {
                out.print(data.getProc().isAlive());
            } else {
                out.print(false);
            }
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
        return "Get if the current process is still running.";
    }// </editor-fold>

}
