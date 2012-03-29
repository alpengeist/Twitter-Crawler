package de.alpengeist;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class NeoFeeder {
    private Logger log = LoggerFactory.getLogger(NeoFeeder.class);
    private GraphDatabaseService db;
    private Index<Node> nodeIndex, nameIndex;
    private RelationshipIndex friendIndex;
    private int nodeCount, edgeCount;

    public NeoFeeder(String dbpath) {
        db = new EmbeddedGraphDatabase(dbpath);
        nodeIndex = db.index().forNodes(Prop.TWITTER_ID.toString());
        nameIndex = db.index().forNodes(Prop.NAME.toString());
        friendIndex = db.index().forRelationships(TwitterRelationship.FRIEND.toString());
        registerShutdownHook();
    }


    public void shutdown() {
        db.shutdown();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public interface Trx {
        public void execute(GraphDatabaseService db) throws Exception;
    }

    public GraphDatabaseService getDatabase() {
        return db;
    }

    public void transaction(Trx trx) {
        Transaction trans = db.beginTx();
        try {
            trx.execute(db);
            trans.success();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Transaction failed", e);
            throw new RuntimeException(e);
        } finally {
            trans.finish();
        }

    }

    public Node createUserIfNotExists(long twitterId) {
        Node node = findUser(twitterId);
        if (node == null) {
            node = createUser(twitterId);
        }
        return node;
    }

    public Node createUser(long twitterId) {
        Node node = db.createNode();
        Prop.TWITTER_ID.set(node, twitterId);
        nodeIndex.add(node, Prop.TWITTER_ID.toString(), twitterId);
        log.info("created user {}", twitterId);
        nodeCount++;
        return node;
    }

    public void addToNameIndex(Node node) {
        String name = Prop.NAME.getS(node);
        if (!StringUtils.isEmpty(name)) {
            nameIndex.add(node, Prop.NAME.toString(), name);
        }
    }


    public Node findUser(long twitterId) {
        return nodeIndex.get(Prop.TWITTER_ID.toString(), twitterId).getSingle();
    }

    public Relationship createFriendIfNotExists(Node from, Node to) {
        Relationship relationship = findFriendship(from, to);
        if (relationship == null) {
            relationship = createFriend(from, to);
        }
        return relationship;
    }

    public Relationship createFriend(Node from, Node to) {
        Relationship rela = from.createRelationshipTo(to, TwitterRelationship.FRIEND);
        friendIndex.add(rela, "follows", makeFriendIndexKey(from, to));
        log.info("created friend {} -> {}", Prop.TWITTER_ID.getL(from), Prop.TWITTER_ID.getL(to));
        edgeCount++;
        return rela;
    }

    public Relationship findFriendship(Node from, Node to) {
        IndexHits<Relationship> hits = friendIndex.get("follows", makeFriendIndexKey(from, to));
        return hits.getSingle();
    }

    private String makeFriendIndexKey(Node from, Node to) {
        return Prop.TWITTER_ID.getL(from) + "-" + Prop.TWITTER_ID.getL(to);
    }

    public static void deleteDatabase(String dbpath) {
        try {
            FileUtils.deleteDirectory(new File(dbpath));
        } catch (IOException e) {
            throw new IllegalStateException("Database directory " + dbpath + " cannot be deleted: " + e.getMessage());
        }
    }

    public void initializeGraph() {
        transaction(new Trx() {
            @Override
            public void execute(GraphDatabaseService db) {
                Node root = db.getReferenceNode();
                if (! root.hasRelationship(TwitterRelationship.ALPENGEIST)) {
                    Node nodeNumberOne = createUser(Config.MY_TWITTER_ID);
                    Prop.DEPTH.set(nodeNumberOne, 1L);
                    root.createRelationshipTo(nodeNumberOne, TwitterRelationship.ALPENGEIST);
                } else {
                    log.info("Alpengeist exists");
                }
            }
        });
    }

    public Node getNodeNumberOne() {
        return db.getNodeById(1);
    }

    public void logStatistics() {
        log.info("created {} nodes and {} edges", nodeCount, edgeCount);
    }

}
