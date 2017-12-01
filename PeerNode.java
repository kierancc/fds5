/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * This class represents a peer within the network. The peer class is rather stupid (just for data storage). Network logic shall be implemented in a subclass of Network.
 *
 * @author Gert Brettlecker
 * @author Christoph Langguth
 */
public abstract class PeerNode {

    /**
     * The nodeID of this peer.
     */
    protected final String nodeID;

    /**
     * Reference to the network this peer belongs to.
     */
    protected final Network network;

    /**
     * Local data stored at the peer.
     */
    protected final Map<String, String> localData = new TreeMap<String, String>();

    /**
     * Connections to other peers known by this peer.
     */
    protected final Map<String, Integer> connections = new HashMap<String, Integer>();


    /**
     * Constructor
     *
     * @param nodeID id of the peer
     */
    public PeerNode( Network network, String nodeID ) {
        this.network = network;
        this.nodeID = nodeID;
    }


    /**
     * Implement this in subclass to save data items at the node.
     *
     * @param originOfQuery the node calling the method (purely for logging purposes). Null if it's the client (i.e., not a node in the network)
     * @param key of data item
     * @param value of data item
     */
    public abstract void setDataItem( PeerNode originOfQuery, String key, String value );

    /**
     * Implement this in subclass to get a data item stored at this node.
     *
     * @param originOfQuery is null if the query comes from client otherwise the first peer in the network
     * @param key of data item
     * @return value of data item
     */
    public abstract String getDataItem( PeerNode originOfQuery, String key );

    /**
     * Implement this in subclass to determine the node where the data is/should be located.
     *
     * @param originOfQuery is null if the query comes from client otherwise the first peer in the network
     * @param key of data item
     * @return value of data item
     */
    public abstract PeerNode lookupNodeForItem( PeerNode originOfQuery, String key );


    /**
     * Check if data item exists at this peer.
     *
     * @param key of data item
     * @return true if exists
     */
    public boolean hasDataItem( String key ) {
        return localData.containsKey( key );
    }


    /**
     * Get node id of this peer.
     *
     * @return node id
     */
    public String getNodeID() {
        return nodeID;
    }


    /**
     * Save connection to other peer at this peer.
     *
     * @param toId node id of other peer
     */
    public final void addConnection( String toId ) {
        // ignore "connections" to ourselves.
        if ( toId.equals( nodeID ) ) {
            return;
        }
        synchronized ( connections ) {
            Integer count = connections.get( toId );
            if ( count == null ) {
                count = Integer.valueOf( 0 );
            }
            connections.put( toId, count.intValue() + 1 );
        }
    }


    /**
     * @param toId node id of other peer
     */
    public final void removeConnection( String toId ) {
        synchronized ( connections ) {
            Integer count = connections.get( toId );
            if ( count == null ) {
                // this shouldn't happen, but you never know
                return;
            }
            int newCount = count.intValue() - 1;
            if ( newCount == 0 ) {
                connections.remove( toId );
            } else {
                connections.put( toId, newCount );
            }
        }
    }


    /**
     * Check if peer has connection to other peer.
     *
     * @param toID node id of other peer
     * @return true if connection exists
     */
    public final boolean hasConnectionTo( String toID ) {
        synchronized ( connections ) {
            return connections.containsKey( toID );
        }
    }


    /**
     * Return all connections of this peer.
     *
     * @return ArrayList containing connections
     */
    public final Set<String> getConnections() {
        synchronized ( connections ) {
            return Collections.unmodifiableSet( connections.keySet() );
        }
    }


    @Override
    public String toString() {
        return getNodeID() + " - " + network.hash( getNodeID() );
    }


    /**
     * Gets the local data.
     *
     * @return the local data
     */
    public final Map<String, String> getLocalData() {
        return localData;
    }


    /**
     * Gets the chord predecessor. This method only makes sense for Network implementations, others should return null (not overriding this).
     *
     * @return the chord predecessor
     */
    public PeerNode getChordPredecessor() {
        return null;
    }


    /**
     * Dump chord finger table. This method only makes sense for Network implementations, others should return null (not overriding this).
     *
     * @return the formatted string representing the finger table
     */
    public String dumpChordFingerTable() {
        return null;
    }
}
