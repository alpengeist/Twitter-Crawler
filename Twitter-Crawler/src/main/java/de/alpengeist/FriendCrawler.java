package de.alpengeist;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.TwitterException;

import java.io.IOException;

public class FriendCrawler extends TwitterCrawler {
    private Logger log = LoggerFactory.getLogger(FriendCrawler.class);

    public FriendCrawler() throws IOException {
        super();
    }

    private static final long[] EMPTY_IDS = new long[0];
    public long[] getFriendIds(long twitterId) throws TwitterException {
        NodeCache.Entry e = nodeCache.get(twitterId);
        long[] ids;
        if (e == null) {
            // it makes no sense to call twitter in this run if the rate has exceeded
            if (isRateLimitExceeded) {
                ids = EMPTY_IDS;
            } else {
                ids = twitter().getFriendsIDs(twitterId, -1).getIDs();
                nodeCache.put(twitterId, ids);
            }
        } else {
            ids = e.friendIds;
        }
        return ids;
    }

    public void crawlFriends(final long twitterId) {
        neo.transaction(new NeoFeeder.Trx() {
            @Override
            public void execute(GraphDatabaseService db) throws Exception {
                Node root = findUser(twitterId);
                try {
                    crawlFriends(root, 1);
                } catch (TwitterException e) {
                    // save the work, just report and finish
                    log.warn("Twitter error", e);
                } finally {
                    nodeCache.writeCache();
                }
            }
        });
    }

    private void crawlFriends(Node node, int depth) throws TwitterException {
        if (depth < maxdepth) { // lookahead: the friends are maxdepth
//            if (!node.hasRelationship(TwitterRelationship.FRIEND))
                createFriendsForNode(node, depth + 1);
            // width has already been limited by createFriendsForNode
            for (Relationship rel :  node.getRelationships(TwitterRelationship.FRIEND, Direction.OUTGOING)) {
                crawlFriends(rel.getEndNode(), depth + 1);
            }
        }
    }

    private void createFriendsForNode(Node node, int depth) throws TwitterException {
        try {
            long[] ids = getFriendIds(Prop.TWITTER_ID.getL(node));
            log.info("creating {} friends for {}", Math.min(ids.length, maxwidth) , Prop.TWITTER_ID.getL(node));
            for (int friendIndex = 0; friendIndex < maxwidth && friendIndex < ids.length; friendIndex++) {
                // new node created without data
                Node friend = neo.findUser(ids[friendIndex]);
                if (friend == null) {
                    friend = neo.createUser(ids[friendIndex]);
                    Prop.DEPTH.set(friend, depth);
                    Prop.WIDTH.set(friend, friendIndex + 1);
                }
                neo.createFriendIfNotExists(node, friend);
            }
        } catch (TwitterException e) {
            if (e.getStatusCode() == 401) {
                // only skip this guy
                log.warn("cannot get friends of {} - authorization required", Prop.TWITTER_ID.getL(node));
            } else if (e.getStatusCode() == 400) {
                // most likely request rate limit; continue since cache can still feed following nodes
                isRateLimitExceeded = true;
                log.warn("twitter rate limit exceeded - skipping further requests");
            }
        }
    }
}