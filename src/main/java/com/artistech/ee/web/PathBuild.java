/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.green;

//import com.artistech.ee.beans.Data;
import com.artistech.ee.beans.DataBase;
import com.artistech.ee.beans.DataManager;
import com.artistech.ee.beans.PipelineBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author matta
 */
@WebServlet(name = "PathBuild", urlPatterns = {"/PathBuild"})
public class PathBuild extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int MAX_MEMORY_SIZE = 1024 * 1024 * 2;
    private static final int MAX_REQUEST_SIZE = 1024 * 1024;
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (!isMultipart) {
            return;
        }

        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Sets the size threshold beyond which files are written directly to
        // disk.
        factory.setSizeThreshold(MAX_MEMORY_SIZE);

        // Sets the directory used to temporarily store files that are larger
        // than the configured size threshold. We use temporary directory for
        // java
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        // constructs the folder where uploaded file will be stored
        String uploadFolder = getServletContext().getInitParameter("data_path");

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Set overall request size constraint
        upload.setSizeMax(MAX_REQUEST_SIZE);

        //do work...
        Part part = request.getPart("pipeline_id");
        String pipeline_id = IOUtils.toString(part.getInputStream(), "UTF-8");
        DataManager dataManagerBean = new DataManager();
        dataManagerBean.setPipeline_id(pipeline_id);

        DataBase data = (DataBase) DataManager.getData(pipeline_id);
        if (data == null) {
            data = DataManager.newDataInstance(pipeline_id);
        }

        data.setPipelineDir(uploadFolder);

        dataManagerBean.setData(data);

        part = request.getPart("step_name");
        String stepName = IOUtils.toString(part.getInputStream(), "UTF-8");
        PipelineBean pb = new PipelineBean();
        PipelineBean.Part create = pb.createPart(stepName);

        for (PipelineBean.Parameter p : create.getParameters()) {
            part = request.getPart(stepName + "__" + p.getName());
            if (part != null) {
                //HANDLE THE INPUT!
                System.out.println(p.getName());

                /**
                 * Handle an enumerated (dropdown/select).
                 */
                if (p.getType().equals("select")) {
                    String value = IOUtils.toString(part.getInputStream(), "UTF-8");
                    p.setValue(value);
                }

                /**
                 * Handle Uploading a File.
                 */
                if (p.getType().equals("file")) {
                    // be sure there is a file that was uploaded.
                    if ("".equals(part.getSubmittedFileName().trim())) {
                        getServletContext().getRequestDispatcher("/pipeline.jsp?pipeline_id=" + pipeline_id).forward(
                                request, response);
                        return;
                    }
                    p.setValue(part.getSubmittedFileName());

                    File dir = new File(data.getInput());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    String submittedFileName = part.getSubmittedFileName();
                    if ("".equals(submittedFileName)) {
                        // displays done.jsp page after upload finished
//                        getServletContext().getRequestDispatcher("/pipeline.jsp?pipeline_id=" + pipeline_id).forward(
//                                request, response);
                        MAPPER.writeValue(response.getOutputStream(), data.getCurrentParts());
                        return;
                    }

                    File f = new File(data.getInput() + File.separator + part.getSubmittedFileName());
                    if (f.exists()) {
                        f.delete();
                    }
                    try (FileOutputStream fos = new FileOutputStream(f)) {
                        IOUtils.copy(part.getInputStream(), fos, 1024);
                    }
                }
            }
        }
        data.addPart(create);

        MAPPER.writeValue(response.getOutputStream(), data.getCurrentParts());
//        getServletContext().getRequestDispatcher("/pipeline.jsp?pipeline_id=" + pipeline_id).forward(
//                request, response);
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
        return "Short description";
    }// </editor-fold>

}
