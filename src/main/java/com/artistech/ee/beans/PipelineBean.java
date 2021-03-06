/*
 * Copyright 2017 ArtisTech, Inc.
 */
package com.artistech.ee.beans;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author matta
 */
public class PipelineBean {

    private Map map;
    private final Map<String, Part> parts;
    private int index;

    public static final PipelineBean INSTANCE;

    static {
        INSTANCE = new PipelineBean();
    }

    public class Part implements Comparable<Part> {

        private final boolean mutiRun;
        private final String name;
        private final Map<String, Parameter> params;
        private final String page;
        private final String[] requires;
        private String outputDir;

        public Part(Part copy) {
            this.outputDir = copy.outputDir;
            this.name = copy.name;
            this.mutiRun = copy.mutiRun;
            this.page = copy.page;
            this.requires = copy.requires;
            params = new HashMap<>();
            for (String key : copy.params.keySet()) {
                this.params.put(key, copy.params.get(key).copy());
            }
        }

        public Part(String name, Map map) {
            this.name = name;
            params = new HashMap<>();
            outputDir = map.get("output-dir").toString();
            page = map.get("page").toString();
            ArrayList paramsObj = (ArrayList) map.get("parameters");
            mutiRun = map.containsKey("multi-run") ? Boolean.parseBoolean(map.get("multi-run").toString()) : false;
            for (Object param : paramsObj) {
                Map m = (Map) ((Map) param).get("parameter");
                Parameter p = new Parameter(m.get("name").toString(), m);
                params.put(p.getName(), p);
            }
            ArrayList list = (ArrayList) map.get("requires");
            requires = (String[]) list.toArray(new String[]{});
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String value) {
            outputDir = value;
        }

        public String[] getRequires() {
            return requires;
        }

        public String getPage() {
            return page;
        }

        public String getName() {
            return this.name;
        }

        public Parameter[] getParameters() {
            ArrayList<Parameter> vals = new ArrayList<>(params.values());
            Collections.sort(vals);
            return vals.toArray(new Parameter[]{});
        }

        public Parameter getParameter(String name) {
            return params.get(name);
        }

        Part copy() {
            return new Part(this);
        }

        @Override
        public int compareTo(Part t) {
            return this.name.compareTo(t.name);
        }
    }

    public class Parameter implements Comparable<Parameter> {

        private final String name;
        private String value;
        private final ArrayList<String> values;
        private final String type;

        public Parameter(Parameter copy) {
            this.name = copy.name;
            this.value = copy.value;
            this.values = new ArrayList<>(copy.values);
            this.type = copy.type;
        }

        public Parameter(String name, Map map) {
            type = map.get("type").toString();
            this.name = name;
            values = new ArrayList<>();
            ArrayList vals = (ArrayList) map.get("values");
            if (vals != null) {
                for (Object obj : vals) {
                    values.add(obj.toString());
                }
            }
            value = map.get("value").toString();
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String[] getValues() {
            return values.toArray(new String[]{});
        }

        public String getType() {
            return type;
        }

        Parameter copy() {
            return new Parameter(this);
        }

        @Override
        public int compareTo(Parameter t) {
            return this.name.compareTo(t.name);
        }
    }

    public PipelineBean() {
        map = PipelineBean.getPipelineConfig();
        parts = new HashMap<>();
        Map get = (Map) map.get("parts");
        for (Object key : get.keySet()) {
            Part p = new Part(key.toString(), (Map) get.get(key));
            parts.put(key.toString(), p);
        }
    }

    public String getName() {
        return map.get("name").toString();
    }

    public String getDescription() {
        return map.get("description").toString();
    }

    public String getDataBeanType() {
        return map.get("data-object").toString();
    }

    public String getDataDir() {
        return map.get("data-path") != null ? map.get("data-path").toString() : null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int value) {
        this.index = value;
    }

    public Part[] getParts() {
        ArrayList<Part> vals = new ArrayList<>(parts.values());
        Collections.sort(vals);
        return vals.toArray(new Part[]{});
    }

    public Part[] getPartsAfter(ArrayList<String> done) {
        ArrayList<Part> ret = new ArrayList<>();
        for (Part p : parts.values()) {
            List<String> reqs = Arrays.asList(p.getRequires());
            Collection<String> subtract = CollectionUtils.subtract(reqs, done);
            if (subtract.isEmpty()) {
                if (p.mutiRun || !done.contains(p.getName())) {
                    ret.add(p);
                }
            }
        }
        Collections.sort(ret);
        return ret.toArray(new Part[]{});
    }

    public Part[] getPartsAfter(String[] after) {
        List<String> done = Arrays.asList(after);
        ArrayList<Part> ret = new ArrayList<>();
        for (Part p : parts.values()) {
            List<String> reqs = Arrays.asList(p.getRequires());
            Collection<String> subtract = CollectionUtils.subtract(reqs, done);
            if (subtract.isEmpty()) {
                if (p.mutiRun || !done.contains(p.getName())) {
                    ret.add(p);
                }
            }
        }
        return ret.toArray(new Part[]{});
    }

    public Part createPart(String name) {
        return parts.get(name).copy();
    }

    public static Map getPipelineConfig() {
        ServiceLoader<PipelineYamlUpdater> loader = ServiceLoader.load(PipelineYamlUpdater.class);
        URL resource = Thread.currentThread().getContextClassLoader().getResource("pipeline.yml");
        Map map = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(resource.openStream()))) {
            YamlReader reader = new YamlReader(in);
            Object object = reader.read();
            map = (Map) object;

            for (PipelineYamlUpdater updater : loader) {
                updater.updatePipeline(map);
            }
        } catch (IOException ex) {
            Logger.getLogger(PipelineBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }
}
