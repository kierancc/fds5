/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.DefaultOption;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.help.Version;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;


@Command(name = "fds-p2p", description = "Peer-to-Peer exercise for the lecture Foundations of Distributed Systems.")
@Version(sources = { "/META-INF/MANIFEST.MF" }, versionProperty = "Version", suppressOnError = true)
public class Main {

    private static final Logger log = Logger.getLogger( Main.class.getSimpleName() );

    static final int DEBUG_INITIAL_NODES = 5;
    static final int DEBUG_NETWORK_BITS = 3;

    @Inject
    public HelpOption help;

    @Option(name = { "--debug" }, description = "Enable debug mode. This also reduces the number of nodes (to " + DEBUG_INITIAL_NODES + ") and network bits (to " + DEBUG_NETWORK_BITS + ") by overwriting the values.")
    private boolean debug = false;

    @Option(name = { "-fcn", "--fullyConnectedNetwork" }, description = "If this option is present the FullyConnectedNetwork and the FullyConnectedPeer classes will be used. Otherwise the ChordNetwork is used.")
    private boolean fcn = false;

    @Option(name = { "--initialNodes" }, description = "Number of initial nodes. If '--debug' is present this value is overwritten. Default: 10")
    private int initialNodes = 10;

    @Option(name = { "--networkBits" }, description = "Number of network bits. If '--debug' is present this value is overwritten. Default: 24")
    private int networkBits = 24;

    @Option(name = { "--fingerUpdateInterval" }, description = "Update interval in milliseconds. Default: 2000")
    private int fingerUpdateInterval = 2000; // Guess in ms
    
    @Option(name = { "--stabilize" }, description = "Stabilize interaval for each node in milliseconds. Default 2000")
    private int stabilizeInterval = 2000;

    @Option(name = { "--useSuccessorOnly" }, description = "Use only the successor relation an not the finger table.")
    private boolean useSuccessorOnly = false;

    @Option(name = { "-cpc", "--chordPeerClass" }, description = "Name of the class which is used for the chord peers. Default: 'ch.unibas.dmi.dbis.fds.p2p.ChordPeerImpl'")
    @DefaultOption
    private String chordPeerClassName = ChordPeerImpl.class.getCanonicalName();


    public static void main( String[] args ) {
        SingleCommand<Main> parser = SingleCommand.singleCommand( Main.class );
        Main main = parser.parse( args );

        if ( !main.help.showHelpIfRequested() ) {
            if ( main.debug ) {
                main.initialNodes = DEBUG_INITIAL_NODES;
                main.networkBits = DEBUG_NETWORK_BITS;
            }

            if ( main.fcn ) {
                main.chordPeerClassName = FullyConnectedPeer.class.getName();
            }

            main.run();
        }
    }


    public Main() {
    }


    private void run() {
        try {

            final Class<? extends ChordPeerNode> chordPeerClass = (Class<? extends ChordPeerNode>) Class.forName( chordPeerClassName );

            final Network network;
            if ( fcn ) {
                network = Network.newFullyConnectedNetwork( networkBits );
            } else {
                network = Network.newChordNetwork( networkBits, chordPeerClass );
            }

            GuiFrame frame = new GuiFrame( network, fingerUpdateInterval, stabilizeInterval, useSuccessorOnly );

            frame.setTitle( "Peer-2-Peer Exercise" );
            frame.setVisible( true );

            for ( int i = 0; i < initialNodes; i++ ) {
                frame.createPeer( "Node_" + i );
            }

            //Create the overlay structure of the network
            network.arrangeOverlayStructure();

            //save an entry at a random node
            String key = "Test";
            PeerNode node = network.getRandomPeer().lookupNodeForItem( null, key );

            node.setDataItem( null, key, "Value" );

            // re-reretrieve the entry starting from a random node
            node = network.getRandomPeer().lookupNodeForItem( null, key );
            node.getDataItem( null, key );

            //draw the network
            frame.updateLog( null );
            frame.repaint();

        } catch ( Throwable t ) {
            log.log( Level.SEVERE, "Uncaught exception", t );
        }
    }
}
