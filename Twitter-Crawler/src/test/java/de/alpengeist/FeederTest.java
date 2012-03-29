package de.alpengeist;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class FeederTest {
    private static final String DBPATH = "D:/javadev/TwitterCrawler/TwitterDB/db";
    private NeoFeeder neo;

    @BeforeTest public void start() {
        NeoFeeder.deleteDatabase(DBPATH);
        neo = new NeoFeeder(DBPATH);
        neo.initializeGraph();
    }

    @AfterTest public void shutdown() {
        neo.shutdown();
    }

    @Test
    public void test1() {
        neo.transaction(new NeoFeeder.Trx() {
            public void execute(GraphDatabaseService db) {
                Node
                        f1 = neo.createUser(1);
                Prop.NAME.set(f1, "Hans");
                neo.createFriendIfNotExists(neo.getNodeNumberOne(), f1);
                assertNotNull(neo.findUser(1));
                Node f2 = neo.createUserIfNotExists(1);
                assertEquals(f2.getProperty(Prop.NAME.toString()), "Hans");

                assertNotNull(neo.findFriendship(neo.getNodeNumberOne(), f1));
                assertNull(neo.findFriendship(f1, neo.getNodeNumberOne()));
            }
        });
    }
}
