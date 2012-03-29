package de.alpengeist;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.io.IOException;

public abstract class TwitterCrawler {
    private Logger log = LoggerFactory.getLogger(TwitterCrawler.class);
    protected NeoFeeder neo;
    protected NodeCache nodeCache;
    protected DataCache dataCache;
    protected boolean isRateLimitExceeded;
    protected int maxwidth = MAXWIDTH, maxdepth = MAXDEPTH;
    private Twitter twitter;

    protected int twitterAccessCount;

    private static final int
            MAXDEPTH = 4,
            MAXWIDTH = 50
            ;

    public TwitterCrawler() throws IOException {
        TwitterFactory tf = new TwitterFactory(Config.get());
        twitter = tf.getInstance();
        neo = new NeoFeeder(Config.NEO_DB_PATH);
        nodeCache = new NodeCache(Config.TWITTER_NODE_CACHE_PATH);
        dataCache = new DataCache(Config.TWITTER_DATA_CACHE_PATH);
        neo.initializeGraph();
    }

    public void setMaxwidth(int max) {
        maxwidth = max;
    }

    public void setMaxdepth(int max) {
        maxdepth = max;
    }

    public Twitter twitter() {
        twitterAccessCount++;
        return twitter;
    }

    public void logStatistics() {
        log.info("required {} Twitter requests. Rate limit exceeded={}", twitterAccessCount, isRateLimitExceeded);
        neo.logStatistics();
    }

    protected Node findUser(long twitterId) {
        Node node = neo.findUser(twitterId);
        if (node == null) {
            throw new IllegalStateException(twitterId + " not found in DB");
        }
        return node;
    }

}
