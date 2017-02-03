/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.web;

//import com.artistech.ee.beans.Data;
import com.artistech.ee.beans.DataBase;
import com.artistech.ee.beans.DataManager;
import com.artistech.ee.beans.PipelineBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.event.EventListenerSupport;

/**
 * Add a new step in the process path.
 *
 * @author matta
 */
@WebServlet(name = "PathBuild", urlPatterns = {"/PathBuild"})
public class PathBuild extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int MAX_MEMORY_SIZE = 1024 * 1024 * 2;
    private static final int MAX_REQUEST_SIZE = 1024 * 1024;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

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
        Part param_part = request.getPart("pipeline_id");
        String pipeline_id = IOUtils.toString(param_part.getInputStream(), "UTF-8");
        DataManager dataManagerBean = new DataManager();
        dataManagerBean.setPipeline_id(pipeline_id);

        DataBase data = (DataBase) DataManager.getData(pipeline_id);
        if (data == null) {
            data = DataManager.newDataInstance(pipeline_id);
        }

        data.setPipelineDir(uploadFolder);

        dataManagerBean.setData(data);

        param_part = request.getPart("step_name");
        String stepName = IOUtils.toString(param_part.getInputStream(), "UTF-8");
        PipelineBean pb = new PipelineBean();

        Collection<Part> parts = request.getParts();
        for (Part p : parts) {
            Logger.getLogger(PathBuild.class.getName()).log(Level.WARNING, p.getName());
        }

        /**
         * Loop through all pairings. This is a hack due to allowing multiple
         * file uploads from dropzone.
         */
        int last_count = parts.size() + 1;
        ArrayList<Part> toRemove = new ArrayList<>();
        while (parts.size() != last_count) {
            PipelineBean.Part create = pb.createPart(stepName);
            last_count = parts.size();
            for (PipelineBean.Parameter p : create.getParameters()) {
                for (Part part : parts) {
                    if (part.getName().startsWith(stepName + "__" + p.getName())) {
                        toRemove.add(part);
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
                            String submittedFileName = part.getSubmittedFileName();
                            if (submittedFileName == null || "".equals(submittedFileName.trim())) {
                                MAPPER.writeValue(response.getOutputStream(), data.getPipelineParts());
                                return;
                            }
                            // be sure there is a file that was uploaded.
                            submittedFileName = part.getSubmittedFileName();
                            if (submittedFileName == null || "".equals(submittedFileName.trim())) {
                                MAPPER.writeValue(response.getOutputStream(), data.getPipelineParts());
                                return;
                            }
                            p.setValue(submittedFileName);

                            File dir = new File(data.getInput());
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }

                            File f = new File(data.getInput() + File.separator + submittedFileName);
                            if (f.exists()) {
                                f.delete();
                            }
                            try (FileOutputStream fos = new FileOutputStream(f)) {
                                IOUtils.copy(part.getInputStream(), fos, 1024);
                            }
                            break;
                        }
                    }
                }
                parts.removeAll(toRemove);
            }
            if (create.getParameters().length == 0 || !toRemove.isEmpty()) {
                data.addPart(create);
                toRemove.clear();
            }
        }

        writeConfig(data);
        MAPPER.writeValue(response.getOutputStream(), data.getPipelineParts());
    }

    /**
     * Write out the config.
     *
     * @param data
     */
    private void writeConfig(DataBase data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(data.getConfigFile())))) {
            MAPPER.writeValue(bw, data.getPipelineParts());
        } catch (IOException ex) {
            Logger.getLogger(PathBuild.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Add a new step in the process path.";
    }// </editor-fold>

}
