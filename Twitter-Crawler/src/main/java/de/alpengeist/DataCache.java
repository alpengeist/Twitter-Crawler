package de.alpengeist;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class DataCache {
    private Logger log = LoggerFactory.getLogger(DataCache.class);
    private Properties props;
    private File file;

    public DataCache(String filePath) throws IOException {
        file = new File(filePath);
        loadCache();
    }

    private void loadCache() throws IOException {
        try {
            props = new Properties();
            props.load(new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            log.info("data cache does not exist");
        }
    }

    public void put(long twitterId, Prop prop, Object obj) {
        if (obj != null) {
            props.setProperty(twitterId + "." + prop.name(), obj.toString());
        }
    }

    public String get(long twitterId, Prop prop) {
        return props.getProperty(twitterId + "." + prop.name(), null);
    }
    
    public void putNode(Node node) {
        long twitterId = Prop.TWITTER_ID.getL(node);
        for (Prop prop : Prop.values()) {
            switch(prop.getType()) {
                case LONG:
                    putLong(twitterId, node, prop);
                    break;
                case STRING:
                    putString(twitterId, node, prop);
                    break;
                case DOUBLE:
                    putDouble(twitterId, node, prop);
                    break;
                default:
                    log.error("Prop type {} not handled", prop.getType());
            }
        }
    }
    
    public void fillNode(Node node) {
        long twitterId = Prop.TWITTER_ID.getL(node);
        log.info("filling {} from cache", twitterId);
        for (Prop prop : Prop.values()) {
            switch(prop.getType()) {
                case LONG:
                    fillLong(twitterId, node, prop);
                    break;
                case STRING:
                    fillString(twitterId, node, prop);
                    break;
                case DOUBLE:
                    fillDouble(twitterId, node, prop);
                    break;
                default:
                    log.error("Prop type {} not handled", prop.getType());
            }
        }
    }

    public boolean hasNode(Node node) {
        return get(Prop.TWITTER_ID.getL(node), Prop.NAME) != null;
    }
   
    private void putString(long twitterId, Node node, Prop prop) {
        if (prop.exists(node)) {
            put(twitterId, prop, prop.getS(node));
        }
    }

    private void putLong(long twitterId, Node node, Prop prop) {
        if (prop.exists(node)) {
            put(twitterId, prop, prop.getL(node));
        }
    }

    private void putDouble(long twitterId, Node node, Prop prop) {
        if (prop.exists(node)) {
            put(twitterId, prop, prop.getD(node));
        }
    }


    private void fillString(long twitterId, Node node, Prop prop) {
        prop.set(node, get(twitterId, prop));
    }

    private void fillLong(long twitterId, Node node, Prop prop) {
        String n = get(twitterId, prop);
        if (n != null) {
            prop.set(node, Long.parseLong(n));
        }
    }

    private void fillDouble(long twitterId, Node node, Prop prop) {
        String n = get(twitterId, prop);
        if (n != null) {
            prop.set(node, Double.parseDouble(n));
        }
    }

    public void writeCache() throws IOException {
        writeCache(file);
    }

    public void writeCache(File outfile) throws IOException {
        log.info("writing cache to {}", outfile.getAbsolutePath());
        props.store(new BufferedWriter(new FileWriter(outfile)), null);

    }
}
