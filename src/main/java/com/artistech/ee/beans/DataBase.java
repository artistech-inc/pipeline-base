/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.beans;

import com.artistech.utils.ExternalProcess;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author matta
 */
public abstract class DataBase {

    public static final String INPUT_DIR = "input";

    public String dataDir = "";
    private final ArrayList<PipelineBean.Part> path;
    private int index;

    private Calendar last_use;
    private final String key;
    private ExternalProcess proc;

    protected DataBase(String key) {
        this.key = key;
        last_use = Calendar.getInstance();
        path = new ArrayList<>();
        index = 1; //assume that step 0 is input!!
    }

    /**
     * Set when the Data was last accessed.
     *
     * @return
     */
    public final Calendar updateLastUse() {
        last_use = Calendar.getInstance();
        return getLastUse();
    }

    /**
     * Get when the data was last accessed.
     *
     * @return
     */
    public final Calendar getLastUse() {
        return (Calendar) last_use.clone();
    }

    public final String getKey() {
        return key;
    }

    public final String getPipelineDir() {
        return dataDir;
    }

    public final void setPipelineDir(String value) {
        dataDir = value + File.separator + key;
        File f = new File(dataDir);
        f.mkdirs();
    }

    public final String getInput() {
        return getPipelineDir() + File.separator + INPUT_DIR;
    }

    public final String[] getInputFiles() {
        File f = new File(getInput());
        if (f.exists()) {
            return f.list();
        }
        return new String[]{};
    }

    public final String[] getFiles(String key) {
        File f = new File(getData(key));
        if (f.exists() && f.isDirectory()) {
            return f.list();
        }
        return new String[]{};
    }

    public final String getData(String key) {
        return getPipelineDir() + File.separator + key;
    }

    public String[] getRunKeys() {
        ArrayList<String> runPath = new ArrayList<>();
        for(PipelineBean.Part part : this.path) {
            runPath.add(part.getOutputDir());
        }
        return runPath.toArray(new String[]{});
    }

    public abstract String[] getKeys();

    public final ExternalProcess getProc() {
        return proc;
    }

    public final void setProc(ExternalProcess value) {
        index += 1;
        proc = value;
    }
    
    public final ArrayList<PipelineBean.Part> getPipelineParts() {
        return path;
    }

    public final void setPipelineParts(ArrayList<PipelineBean.Part> parts) {
        path.clear();
        path.addAll(parts);
    }
    
    public final void addPart(PipelineBean.Part part) {
        path.add(part);
    }

    public final ArrayList<String> getCurrentPath() {
        ArrayList<String> ret = new ArrayList<>();
        for (PipelineBean.Part p : this.path) {
            ret.add(p.getName());
        }
        return ret;
    }

    public final ArrayList<PipelineBean.Part> getCurrentParts() {
        return path;
    }
    
    public final int getPipelineIndex() {
        return index;
    }

}
