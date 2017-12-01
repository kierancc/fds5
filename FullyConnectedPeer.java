/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


/**
 * Example peer implementation class implements a fully connected peer. GetQueries are done via broadcasting to each node. SaveQueries are done at each node.
 *
 * @author Gert Brettlecker
 */
public class FullyConnectedPeer extends PeerNode {

    /**
     * Constructor
     *
     * @param network network object instance
     * @param nodeID node identifier of this peer
     */
    public FullyConnectedPeer( Network network, String nodeID ) {
        super( network, nodeID );
        network.addPeer( this );
    }


    /**
     * In this fully connected network a data item may be saved at any peer. So no save queries are needed to be passed to other peers.
     *
     * @param originOfQuery the peer that issued the query (null if client app)
     * @param key of data item
     * @param data value of data item
     */
    @Override
    public void setDataItem( PeerNode originOfQuery, String key, String data ) {

        //log save query message
        network.logPassedMessage( Message.MessageType.SET, originOfQuery, this );

        //save data item at destination
        localData.put( key, data );

        //log save query result message
        network.logPassedMessage( Message.MessageType.SET_RESPONSE, originOfQuery, this );
    }


    /**
     * In the fully connected network a broadcast to all peers is sent to get a data item if not already available and query is coming from the client.
     *
     * @param originOfQuery the peer that issued the query (null if client app)
     * @param key of data item
     * @return value of data item
     */
    @Override
    public String getDataItem( PeerNode originOfQuery, String key ) {
        String resData = null;

        //log incoming query message
        network.logPassedMessage( Message.MessageType.GET, originOfQuery, this );

        //Check if data is locally available
        resData = localData.get( key );

        //not local and origin of query is client then pass query message to all connections ("broadcast")
        if ( (resData == null) && (originOfQuery == null) ) {

            //Do broadcast to all
            for ( String nodeId : connections.keySet() ) {
                PeerNode p = network.getPeer( nodeId );
                String broadcastResult = p.getDataItem( this, key );
                //check if result available
                if ( broadcastResult != null ) {
                    //store result of query message
                    resData = broadcastResult;
                    // return on first success
                    break;
                }
            }
        }

        //log result of query message
        network.logPassedMessage( Message.MessageType.GET_RESPONSE, this, originOfQuery );

        return resData;
    }


    /**
     * @see PeerNode#lookupNodeForItem(PeerNode, String)
     */
    @Override
    public PeerNode lookupNodeForItem( PeerNode originOfQuery, String key ) {
        // in this kind of network, there is no notion of a node being "responsible" for a particular item,
        // so we always return the current node. Note that this implies that the getDataItem() method
        // has to "recursively" look up data.

        //log incoming query message
        network.logPassedMessage( Message.MessageType.LOOKUP, originOfQuery, this );
        //log outgoing message
        network.logPassedMessage( Message.MessageType.LOOKUP_RESPONSE, this, originOfQuery );
        return this;
    }
}
