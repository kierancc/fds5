/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.AbstractAction;


@SuppressWarnings("serial")
public class FingerTableUpdateAction extends AbstractAction {

    private final Network network;
    private final Random random = new Random();


    public FingerTableUpdateAction( Network network ) {
        super();
        this.network = network;
    }


    public void actionPerformed( ActionEvent arg0 ) {
        PeerNode node = network.getRandomPeer();
        int index = random.nextInt( network.getNumberOfBits() );
        perform( node, index, index );
    }


    public static void perform( PeerNode node, int from, int to ) {
        ChordPeerNode chord = (ChordPeerNode) node;
        if ( from <= 0 ) {
            from = 0;
        }
        if ( to >= chord.m ) {
            to = chord.m - 1;
        }
        chord.fixFingers( from, to );
    }
}
