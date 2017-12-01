/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


/**
 * Example network implementation class Implements a fully connected network
 *
 * @author Gert Brettlecker
 */
public class FullyConnectedNetwork extends Network {

    /**
     * Constructor
     *
     * @param numberOfBits bits used for the identifier ring
     */
    public FullyConnectedNetwork( int numberOfBits ) {
        super( numberOfBits );
    }


    /**
     * This method implements a fully connected mesh of nodes
     */
    @Override
    public void arrangeOverlayStructure() {
        for ( PeerNode p1 : nodes.values() ) {
            for ( PeerNode p2 : nodes.values() ) {
                if ( p1 != p2 ) {
                    p1.addConnection( p2.getNodeID() );
                }
            }
        }
    }


    @Override
    public PeerNode createPeer( String id, boolean useSuccessorOnly ) {
        return new FullyConnectedPeer( this, id );
    }
}
