/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


/**
 * This class implements a message for logging purposes.
 *
 * @author Gert Brettlecker
 */
public class Message {

    /**
     * Enumeration of possible message types.
     */
    public enum MessageType {
        GET, GET_RESPONSE,
        SET, SET_RESPONSE,
        LOOKUP, LOOKUP_RESPONSE,
        CHORD_GET_SUCCESSOR, CHORD_GET_SUCCESSOR_RESPONSE,
        CHORD_GET_PREDECESSOR, CHORD_GET_PREDECESSOR_RESPONSE,
        CHORD_SET_PREDECESSOR, CHORD_SET_PREDECESSOR_RESPONSE,
        CHORD_FIND_SUCCESSOR, CHORD_FIND_SUCCESSOR_RESPONSE,
        CHORD_FIND_PREDECESSOR, CHORD_FIND_PREDECESSOR_RESPONSE,
        CHORD_CLOSEST_PRECEDING_FINGER, CHORD_CLOSEST_PRECEDING_FINGER_RESPONSE,
        CHORD_NOTIFY, CHORD_NOTIFY_RESPONSE,
        CHORD_STABILIZE, CHORD_STABILIZE_RESPONSE,
    }


    /**
     * Message type of this message.
     */
    private MessageType msgType;

    /**
     * Source node id of message.
     */
    private String sourceNodeId;

    /**
     * Destination node id of message.
     */
    private String destinationNodeId;

    /**
     * Timestamp of message creation.
     */
    private long timestamp;


    /**
     * Constructor
     *
     * @param fromID sender node id
     * @param toID receiver node id
     */
    public Message( MessageType msgType, String fromID, String toID ) {
        this.msgType = msgType;
        this.sourceNodeId = fromID;
        this.destinationNodeId = toID;
        this.timestamp = System.currentTimeMillis();
    }


    /**
     * Returns the receiver node id.
     *
     * @return receiver node id
     */
    public String getDestinationNodeId() {
        return destinationNodeId;
    }


    /**
     * Returns the sender node id.
     *
     * @return sender node id
     */
    public String getSourceNodeId() {
        return sourceNodeId;
    }


    /**
     * Returns the timestamp of creation.
     *
     * @return timestamp of creation
     */
    public long getTimestamp() {
        return timestamp;
    }


    /**
     * Returns the message type of this message.
     *
     * @return message type
     */
    public MessageType getMsgType() {
        return msgType;
    }


    @Override
    public String toString() {
        return timestamp + " " + msgType + " " + sourceNodeId + " -> " + destinationNodeId;
    }
}
