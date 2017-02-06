/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.beans;

import com.artistech.utils.ExternalProcess;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for each pipeline's bean.
 *
 * @author matta
 */
public abstract class DataBase {

    public static final String CONSOLE_LOG = "console.log";
    public static final String CONIFG_JSON = "config.json";
    public static final String INPUT_DIR = "input";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String dataDir = "";

    private final ArrayList<PipelineBean.Part> path;
    private int index;

    private Calendar last_use;
    private final String key;
    private ExternalProcess proc;


    /**
     * Constructor.
     *
     * @param key
     */
    protected DataBase(String key) {
        this.key = key;
        last_use = Calendar.getInstance();
        path = new ArrayList<>();
        index = 0; //assume that step 0 is input!!
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

    /**
     * Get the current pipeline key.
     *
     * @return
     */
    public final String getKey() {
        return key;
    }

    /**
     * Get the writable output directory.
     *
     * @return
     */
    public final String getPipelineDir() {
        return dataDir;
    }

    /**
     * Set the output directory.
     *
     * @param value
     */
    public final void setPipelineDir(String value) {
        dataDir = value + File.separator + key;
        File f = new File(dataDir);
        f.mkdirs();
    }

    /**
     * Get the directory for input files.
     *
     * @return
     */
    public final String getInput() {
        return getPipelineDir() + File.separator + INPUT_DIR;
    }

    /**
     * Get the list of input files.
     *
     * @return
     */
    public final String[] getInputFiles() {
        File f = new File(getInput());
        if (f.exists()) {
            return f.list();
        }
        return new String[]{};
    }

    /**
     * Get the files for the specified type/step.
     *
     * @param key
     * @return
     */
    public final String[] getFiles(String key) {
        File f = new File(getData(key));
        if (f.exists() && f.isDirectory()) {
            ArrayList<String> ret = new ArrayList<>();
            for (File file : f.listFiles()) {
                if (file.isFile()) {
                    ret.add(file.getName());
                }
            }
            return ret.toArray(new String[]{});
        }
        return new String[]{};
    }

    /**
     * Get the data directory.
     *
     * @param key
     * @return
     */
    public final String getData(String key) {
        return getPipelineDir() + File.separator + key;
    }

    /**
     * Get the keys of run steps in the process path.
     *
     * @return
     */
    public String[] getRunKeys() {
        File f = new File(getConfigFile());
        ArrayList<String> runPath = new ArrayList<>();
        runPath.add("");
        if (f.exists()) {
            try {
                ArrayList readTree = MAPPER.readValue(f, ArrayList.class);
                for (Object o : readTree) {
                    Map m = (Map) o;
                    runPath.add(m.get("outputDir").toString());
                }

            } catch (IOException ex) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            for (PipelineBean.Part part : this.path) {
                String value = part.getOutputDir();
                if (!runPath.contains(value)) {
                    runPath.add(value);
                }
            }
        }
        return runPath.toArray(new String[]{});
    }

    /**
     * Get the keys/steps in the process.
     *
     * @return
     */
    public String[] getKeys() {
        ArrayList<String> keys = new ArrayList<>();

        Field[] fields = this.getClass().getFields();
        for (Field f : fields) {
            int modifiers = f.getModifiers();
            if ((modifiers & (Modifier.STATIC | Modifier.FINAL))
                    == (Modifier.STATIC | Modifier.FINAL)) {
                try {
                    keys.add(f.get(null).toString());
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return keys.toArray(new String[]{});
    }

    /**
     * Get the external process.
     *
     * @return
     */
    public final ExternalProcess getProc() {
        return proc;
    }

    /**
     * Set the external process.
     *
     * @param value
     */
    public final void setProc(ExternalProcess value) {
        index += 1;
        proc = value;
    }

    /**
     * Get the process path.
     *
     * @return
     */
    public final ArrayList<PipelineBean.Part> getPipelineParts() {
        return path;
    }

    /**
     * Set the process path.
     *
     * @param parts
     */
    public final void setPipelineParts(ArrayList<PipelineBean.Part> parts) {
        path.clear();
        path.addAll(parts);
    }

    /**
     * Add a new step in the process pipeline.
     *
     * @param part
     */
    public final void addPart(PipelineBean.Part part) {
        path.add(part);
    }

    /**
     * Get the process path as names.
     *
     * @return
     */
    public final ArrayList<String> getCurrentPath() {
        ArrayList<String> ret = new ArrayList<>();
        for (PipelineBean.Part p : this.path) {
            ret.add(p.getName());
        }
        return ret;
    }

    /**
     * Get the current pipeline index.
     *
     * @return
     */
    public final int getPipelineIndex() {
        return index;
    }

    /**
     * Get the file for the console log.
     *
     * @return
     */
    public String getConsoleFile() {
        return getPipelineDir() + File.separator + CONSOLE_LOG;
    }

    /**
     * Get the file for the config file.
     *
     * @return
     */
    public String getConfigFile() {
        return getPipelineDir() + File.separator + CONIFG_JSON;
    }

}
