package de.alpengeist;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class Config {
    public static Configuration get() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setHttpProxyHost("proxy.msg.de").setHttpProxyPort(3128);
        return cb.build();
    }

    public final static String NEO_DB_PATH = "D:/javadev/Twitter-Crawler/TwitterDB/db_crawl";
    public final static String TWITTER_NODE_CACHE_PATH = "D:/javadev/Twitter-Crawler/TwitterDB/twitter_node_cache.csv";
    public final static String TWITTER_DATA_CACHE_PATH = "D:/javadev/Twitter-Crawler/TwitterDB/twitter_data_cache.properties";

    public final static long MY_TWITTER_ID = 391388175;
}
