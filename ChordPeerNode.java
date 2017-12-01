/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


public abstract class ChordPeerNode extends PeerNode {

    /**
     * The number of bits used in the CHORD network.
     */
    protected final int m;

    /**
     * This node's hash.
     */
    protected final long n;


    public ChordPeerNode( Network network, String nodeID, boolean useSuccessorsOnly ) {
        super( network, nodeID );
        this.m = network.getNumberOfBits();
        this.n = network.hash( nodeID );
    }


    /**
     * Slight variant of fix_fingers() (Figure 7, page 7): We can update a batch of the finger table in one go, and we allow "external" definition of which entries to update.
     *
     * @param fromInclusive the first index of the finger table to fix
     * @param toInclusive last index of the finger table to fix
     */
    public abstract void fixFingers( int fromInclusive, int toInclusive );
}
