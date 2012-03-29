package de.alpengeist;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.testng.annotations.Test;

import java.io.IOException;

public class CrawlerTest {
    @Test
    public void testFriendCrawl() throws IOException {
        FriendCrawler crawl = new FriendCrawler();
        crawl.setMaxdepth(4);
        crawl.setMaxwidth(40);
        crawl.crawlFriends(Config.MY_TWITTER_ID);
        crawl.logStatistics();
    }

    @Test
    public void testDataFill() throws IOException {
        DataCrawler crawl = new DataCrawler();
        crawl.setMaxdepth(4);
        crawl.setPlacesCountdown(4000); // how many calls to Yahoo you like to do max
        crawl.updateData(Config.MY_TWITTER_ID);
        crawl.logStatistics();
    }

    @Test
    public void depthRepair() {
        final NeoFeeder neo = new NeoFeeder(Config.NEO_DB_PATH);
        neo.transaction(new NeoFeeder.Trx(){

            @Override
            public void execute(GraphDatabaseService db) throws Exception {
                for (Node node : neo.getDatabase().getAllNodes()) {
                    if (Prop.DEPTH.exists(node)) {
                    Number d = (Number) node.getProperty(Prop.DEPTH.toString());
                    if (d instanceof Integer) {
                        node.setProperty(Prop.DEPTH.toString(), d.longValue());
                    }
                    }
                }
            }
        });
    }
}
