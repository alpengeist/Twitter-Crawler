package de.alpengeist;

import org.neo4j.graphdb.RelationshipType;

public enum TwitterRelationship implements RelationshipType {
    ALPENGEIST,
    FRIEND
    ;
}
