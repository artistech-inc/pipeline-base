/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.beans;

import com.artistech.utils.ExternalProcess;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public class DataBase {

    public static final String INPUT_DIR = "input";
//    public static final String ENIE_DIR = "enie_out";
//    public static final String JOINT_ERE_DIR = "joint_ere_out";
//    public static final String MERGE_DIR = "merge_out";
//    public static final String VISUALIZATION_DIR = "viz_out";
//    public static final String TEST_LIST = "test_list";
    public String dataDir = "";
    private final ArrayList<PipelineBean.Part> path;
    private int index;

    private Calendar last_use;
    private final String key;
//    private final HashMap<String, String> map = new HashMap<>();
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

//    public String getTestList() {
//        return getPipelineDir() + File.separator + TEST_LIST;
//    }

//    public void setTestList(String value) {
//        map.put("test_list", value);
//    }
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

//    public void setInput(String value) {
////        map.put("input", value);
//    }

    public final String[] getInputFiles() {
        File f = new File(getInput());
        if (f.exists()) {
            return f.list();
        }
        return new String[]{};
    }

//    public String getJointEreOut() {
//        return getPipelineDir() + File.separator + JOINT_ERE_DIR;
//    }

//    public void setJointEreOut(String value) {
////        map.put("joint_ere_out", value);
//    }

//    public String[] getJointEreOutFiles() {
//        File f = new File(getJointEreOut());
//        if (f.exists()) {
//            return f.list();
//        }
//        return new String[]{};
//    }

//    public String getEnieOut() {
//        return getPipelineDir() + File.separator + ENIE_DIR;
//    }

//    public void setEnieOut(String value) {
////        map.put("enie_out", value);
//    }

//    public String[] getEnieOutFiles() {
//        File f = new File(getEnieOut());
//        if (f.exists()) {
//            return f.list();
//        }
//        return new String[]{};
//    }

//    public String getMergeOut() {
//        return getPipelineDir() + File.separator + MERGE_DIR;
//    }
//
//    public void setMergeOut(String value) {
////        map.put("merge_out", value);
//    }

//    public String[] getMergedFiles() {
//        File f = new File(getMergeOut());
//        if (f.exists()) {
//            return f.list();
//        }
//        return new String[]{};
//    }

//    public String getVizOut() {
//        return getPipelineDir() + File.separator + VISUALIZATION_DIR;
//    }

//    public void setVizOut(String value) {
////        map.put("viz_out", value);
//    }

//    public String[] getVizFiles() {
//        File f = new File(getVizOut());
//        if (f.exists()) {
//            return f.list();
//        }
//        return new String[]{};
//    }

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

    public String[] getKeys() {
        ArrayList<String> keys = new ArrayList<>();
        Field[] fields = DataBase.class.getFields();
        for (Field f : fields) {
            int modifiers = f.getModifiers();
            if ((modifiers & (Modifier.STATIC | Modifier.FINAL))
                    == (Modifier.STATIC | Modifier.FINAL)) {
                try {
                    keys.add(f.get(null).toString());
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return keys.toArray(new String[]{});
    }

    public static String[] getDataKeys() {
        ArrayList<String> keys = new ArrayList<>();
        Field[] fields = DataBase.class.getFields();
        for (Field f : fields) {
            int modifiers = f.getModifiers();
            if ((modifiers & (Modifier.STATIC | Modifier.FINAL))
                    == (Modifier.STATIC | Modifier.FINAL)) {
                try {
                    keys.add(f.get(null).toString());
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return keys.toArray(new String[]{});
    }

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

//    public final void setPipelineIndex(int value) {
//        index = value;
//    }
}
