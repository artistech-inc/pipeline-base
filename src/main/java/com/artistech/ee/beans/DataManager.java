/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.beans;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Used in tomcat and jetty.
 *
 * This class used to manage pipeline builders.
 *
 * @author matta
 */
public class DataManager {

    private static final TreeMap<String, DataBase> DATAS = new TreeMap<>();
    private static String dataPath;
    private String pipeline_id;

    public static class DataDirectoryInfo {

        private final File file;

        public DataDirectoryInfo(File f) {
            file = new File(dataPath + File.separator + f.getName());
        }

        public File getFile() {
            return file;
        }

        public String getDate() {
            Date lastModified = new Date(file.lastModified());
            String formattedDateString = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(lastModified);
            return formattedDateString;
        }

        public String getConfig() {
            File config = new File(file.getAbsolutePath() + File.separator + DataBase.CONIFG_JSON);
            if (config.exists()) {
                try {
                    return FileUtils.readFileToString(config, Charset.defaultCharset());
                } catch (IOException ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return "";
        }
    }

    static {
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource("pipeline.yml");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(resource.openStream()))) {
                YamlReader reader = new YamlReader(in);
                Object object = reader.read();
                Map map = (Map) object;
                if(map.containsKey("data-path")) {
                    dataPath = map.get("data-path").toString();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public DataManager() {
    }

    public static void setDataPath(String value) {
        dataPath = value;
    }

    public static String getDataPath() {
        return dataPath;
    }

    public static DataBase newDataInstance(String key) {
        DataBase ret = null;
        try {
            Class<?> c = Class.forName(PipelineBean.INSTANCE.getDataBeanType());
            Constructor<?> constructor = c.getConstructor(String.class);
            ret = (DataBase) constructor.newInstance(key);
            return ret;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public synchronized DataDirectoryInfo[] getStoredData() {
        ArrayList<DataBase> ret = new ArrayList<>(DATAS.values());
        HashMap<String, File> stored = new HashMap<>();
        File f = new File(getDataPath());
        if (f.exists()) {
            for (File file : f.listFiles()) {
                stored.put(file.getName(), file);
            }
        }
        for (DataBase data : ret) {
            File fi = new File(data.getKey());
            if (stored.containsKey(fi.getName())) {
                stored.remove(fi.getName());
            }
        }
        for (File key : stored.values()) {
            DataBase d = newDataInstance(key.getName());
            d.setPipelineDir(getDataPath());
            ret.add(d);
            DATAS.put(key.getName(), d);
        }
        ArrayList<DataDirectoryInfo> ids = new ArrayList<>();
        for (DataBase data : ret) {
            ids.add(new DataDirectoryInfo(new File(data.getKey())));
        }
        Collections.sort(ids, new Comparator<DataDirectoryInfo>() {
            @Override
            public int compare(DataDirectoryInfo t, DataDirectoryInfo t1) {
                return Long.compare(t.getFile().lastModified(), t1.getFile().lastModified());
            }
        });
        return ids.toArray(new DataDirectoryInfo[]{});
    }

    /**
     * Get all registered Data objects.
     *
     * @return
     */
    public synchronized ArrayList<DataBase> getAllData() {
        ArrayList<DataBase> ret = new ArrayList<>(DATAS.values());
        return ret;
    }

    /**
     * Get the Data object with the specified id.
     *
     * @param id
     * @return
     */
    public static synchronized DataBase getData(String id) {
        if (id != null && DATAS.containsKey(id)) {
            DataBase data = DATAS.get(id);
            data.updateLastUse();
            return data;
        } else {
            return null;
        }
    }

    /**
     * Set the Data object with the specified id.
     *
     * @param id
     * @param data
     */
    public static synchronized void setData(String id, DataBase data) {
        if (id != null) {
            DATAS.put(id, data);
        }
    }

    /**
     * Check if the Data with the specified object exists.
     *
     * @param id
     * @return
     */
    public static synchronized boolean containsData(String id) {
        return id != null ? DATAS.containsKey(id) : false;

    }

    /**
     * Get the algolink id.
     *
     * @return
     */
    public String getPipeline_id() {
        return pipeline_id;
    }

    /**
     * Set the algolink id.
     *
     * @param value
     */
    public void setPipeline_id(String value) {
        pipeline_id = value;
    }

    /**
     * Get the Data associated with the current algolink id.
     *
     * @return
     */
    public DataBase getData() {
        if (pipeline_id != null && DataManager.containsData(pipeline_id)) {
            DataManager.getData(pipeline_id).updateLastUse();
            return DataManager.getData(pipeline_id);
        } else {
            return null;
        }
    }

    /**
     * Set the Data with the current algolink id.
     *
     * @param value
     */
    public void setData(DataBase value) {
        if (pipeline_id != null) {
            setData(pipeline_id, value);
        }
    }

    /**
     * Remove Data with the specified id.
     *
     * @param id
     */
    public static void removeData(String id) {
        DATAS.remove(id);
    }
}
