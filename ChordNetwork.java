/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;


public class ChordNetwork extends Network {

    private final Class<? extends ChordPeerNode> chordPeerClass;
    private final boolean useSuccessorConstructor;


    /**
     * Constructor
     *
     * @param numberOfBits bits used for the identifier ring
     */
    public ChordNetwork( int numberOfBits, Class<? extends ChordPeerNode> chordPeerClass ) {
        super( numberOfBits );

        if ( Modifier.isAbstract( chordPeerClass.getModifiers() ) || chordPeerClass.isInterface() ) {
            throw new IllegalArgumentException( "chordPeerClass must not be abstract or an interface." );
        }

        boolean useSuccessorConstructor;
        try {
            chordPeerClass.getConstructor( Network.class, String.class, boolean.class );
            useSuccessorConstructor = true;
        } catch ( NoSuchMethodException e ) {
            try {
                chordPeerClass.getConstructor( Network.class, String.class );
                useSuccessorConstructor = false;
            } catch ( NoSuchMethodException e1 ) {
                throw new IllegalArgumentException( "The provided implementation of ChordPeerNode does neither have an constructor with the parameters 'Network, String, boolean' not with the parameters 'Network, String'", e1 );
            }
        }

        this.chordPeerClass = chordPeerClass;
        this.useSuccessorConstructor = useSuccessorConstructor;
    }


    @Override
    final void arrangeOverlayStructure() {
        /* This method does not do anything, as Network nodes keep the network structure intact automatically. */
    }


    @Override
    public PeerNode createPeer( String id, boolean useSuccessorsOnly ) {
        try {
            if ( useSuccessorConstructor ) {
                return chordPeerClass.getConstructor( Network.class, String.class, boolean.class ).newInstance( this, id, useSuccessorsOnly );
            } else {
                return chordPeerClass.getConstructor( Network.class, String.class ).newInstance( this, id );
            }
        } catch ( InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e ) {
            throw new RuntimeException( e );
        }
    }
}
