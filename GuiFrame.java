/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;


/**
 * User interface class of the exercise and contains also the main method.
 *
 * @author Gert Brettlecker
 * @author Christoph Langguth
 */
public class GuiFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 4555744497929666899L;

    private static final Logger log = Logger.getLogger( GuiFrame.class.getSimpleName() );


    /**
     * Creates a peer and registers it in the GUI model.
     *
     * @param id the peer id
     * @return the peer node
     */
    PeerNode createPeer( String id ) {
        PeerNode peer = network.createPeer( id, useSuccessorOnly );
        peersModel.addElement( peer );
        return peer;
    }


    /**
     * Network used for this user interface session.
     */
    private final Network network;
    private final int fingerUpdateInterval;
    private final int stabilizeInterval;
    private final boolean useSuccessorOnly;

    /* GUI elements */
    private final DefaultComboBoxModel peersModel;
    private final JComboBox boxPeers;
    private final JButton buttonSetData;
    private final JButton buttonGetData;
    private final JButton buttonClearLog;
    private final JTextField textKey;
    private final JTextField textValue;
    private final JTextArea textLog;

    /* CHORD only */
    private final JRadioButton buttonFingersPeriodic;
    private final JRadioButton buttonFingersManual;
    private final JButton buttonFingersUpdateAll;
    private final JButton buttonFingersUpdate;
    private final JTextField textFingersInterval;
    private final JTextField textFingersStart;
    private final JTextField textFingersEnd;
    private Timer fingerTableUpdateTimer;


    /**
     * Constructor for user interface
     */
    public GuiFrame( final Network network, final int fingerUpdateInterval, final int stabilizeInterval, final boolean useSuccessorOnly ) {
        super();
        this.network = network;
        this.fingerUpdateInterval = fingerUpdateInterval;
        this.stabilizeInterval = stabilizeInterval;
        this.useSuccessorOnly = useSuccessorOnly;

        this.peersModel = new DefaultComboBoxModel();
        this.boxPeers = new JComboBox( this.peersModel );
        this.buttonSetData = new JButton( "Set" );
        this.buttonGetData = new JButton( "Get" );
        this.buttonClearLog = new JButton( "Clear log" );
        this.textKey = new JTextField( "Test", 20 );
        this.textValue = new JTextField( 20 );
        this.textLog = new JTextArea( 10, 25 );

        this.buttonFingersPeriodic = new JRadioButton( "periodic random" );
        this.buttonFingersManual = new JRadioButton( "manual" );
        this.buttonFingersUpdate = new JButton( "Update" );
        this.buttonFingersUpdateAll = new JButton( "Update All" );
        this.textFingersInterval = new JTextField( Integer.toString( this.fingerUpdateInterval ), 5 );
        this.textFingersStart = new JTextField( "0", 3 );
        this.textFingersEnd = new JTextField( 3 );

        setDefaultCloseOperation( EXIT_ON_CLOSE );
        setBackground( Color.lightGray );
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( network.getPanel(), BorderLayout.NORTH );
        
        
        /* GUI stuff */
        JPanel uiPanel = new JPanel( new BorderLayout() );
        textLog.setEditable( false );
        uiPanel.add( new JScrollPane( textLog ), BorderLayout.CENTER );

        JPanel buttonsPanel = new JPanel();
        BoxLayout buttonsPanelLayout = new BoxLayout( buttonsPanel, BoxLayout.Y_AXIS );
        buttonsPanel.setLayout( buttonsPanelLayout );

        JPanel panel = new JPanel( new FlowLayout() );

        boxPeers.addActionListener( this );
        peersModel.addElement( "Network / random peer" );
        panel.add( boxPeers );
        buttonGetData.addActionListener( this );
        panel.add( buttonGetData );
        buttonSetData.addActionListener( this );
        panel.add( buttonSetData );
        buttonClearLog.addActionListener( this );
        panel.add( buttonClearLog );

        buttonsPanel.add( panel );

        panel = new JPanel( new FlowLayout() );
        panel.add( new JLabel( "key:" ) );
        panel.add( textKey );
        panel.add( new JLabel( "value:" ) );
        panel.add( textValue );
        buttonsPanel.add( panel );

        panel = createFingerPanel();
        if ( panel != null ) {
            buttonsPanel.add( panel );
        }

        uiPanel.add( buttonsPanel, BorderLayout.NORTH );
        getContentPane().add( uiPanel, BorderLayout.CENTER );
        pack();
    }


    private JPanel createFingerPanel() {
        if ( !(network instanceof ChordNetwork) ) {
            return null;
        }
        JPanel outer = new JPanel();
        BoxLayout horizontal = new BoxLayout( outer, BoxLayout.X_AXIS );
        outer.setLayout( horizontal );

        buttonFingersManual.addActionListener( this );
        buttonFingersPeriodic.addActionListener( this );
        buttonFingersUpdate.addActionListener( this );
        buttonFingersUpdateAll.addActionListener( this );

        ButtonGroup choice = new ButtonGroup();
        choice.add( buttonFingersPeriodic );
        choice.add( buttonFingersManual );
        buttonFingersManual.setSelected( true );

        JPanel inner = new JPanel();
        BoxLayout vertical = new BoxLayout( inner, BoxLayout.Y_AXIS );
        inner.setLayout( vertical );
        inner.add( buttonFingersPeriodic );

        JPanel line = new JPanel( new FlowLayout() );
        line.add( new JLabel( "every" ) );
        line.add( textFingersInterval );
        line.add( new JLabel( "ms" ) );
        inner.add( line );
        outer.add( inner );

        inner = new JPanel();
        vertical = new BoxLayout( inner, BoxLayout.Y_AXIS );
        inner.setLayout( vertical );
        inner.add( buttonFingersManual );

        line = new JPanel( new FlowLayout() );
        line.add( new JLabel( "from index" ) );
        line.add( textFingersStart );
        line.add( new JLabel( "to index" ) );
        textFingersEnd.setText( "" + (network.getNumberOfBits() - 1) );
        line.add( textFingersEnd );
        line.add( buttonFingersUpdate );
        line.add( buttonFingersUpdateAll );
        inner.add( line );
        outer.add( inner );

        outer.add( inner );

        fingerTableUpdateTimer = new Timer( fingerUpdateInterval, new FingerTableUpdateAction( network ) );
        fingerTableUpdateTimer.setInitialDelay( fingerUpdateInterval );
        fingerTableUpdateTimer.setRepeats( true );

        return outer;
    }


    /**
     * Get the network object from the user interface
     *
     * @return network object instance
     */
    public Network getNetwork() {
        return network;
    }


    public void actionPerformed( ActionEvent e ) {
        PeerNode node = null;
        Object o = boxPeers.getSelectedItem();
        if ( o instanceof PeerNode ) {
            node = (PeerNode) o;
        }
        if ( e.getSource().equals( buttonGetData ) ) {
            String key = textKey.getText();
            getData( node, key );
        } else if ( e.getSource().equals( buttonSetData ) ) {
            String key = textKey.getText();
            String value = textValue.getText();
            setData( node, key, value );
        } else if ( e.getSource().equals( boxPeers ) ) {
            // the logic is already being done "outside",
            // we just need a trigger for performing it

            // just enable/disable the clear log button as required
            buttonClearLog.setEnabled( !(boxPeers.getSelectedItem() instanceof PeerNode) );
        } else if ( e.getSource().equals( buttonClearLog ) ) {
            network.clearLogs();
            this.createPeer( "TEXT" );
        } else if ( e.getSource().equals( buttonFingersUpdate ) ) {
            updateFingers( node );
        } else if ( e.getSource().equals( buttonFingersUpdateAll ) ) {
            allNodesUpdateFingers();
        }
        buttonFingersUpdate.setEnabled( o instanceof PeerNode && buttonFingersManual.isSelected() );
        updateTimer();
        updateLog( node );
        repaint();
    }


    private void updateTimer() {
        if ( fingerTableUpdateTimer == null ) {
            return;
        }
        boolean shouldRun = buttonFingersPeriodic.isSelected();
        int delay = fingerUpdateInterval;
        try {
            delay = Integer.parseInt( textFingersInterval.getText() );
        } catch ( NumberFormatException e ) {
            log.severe( "Invalid interval value \"" + textFingersInterval.getText() + "\", keeping " + fingerUpdateInterval );
        }
        if ( delay != fingerTableUpdateTimer.getDelay() ) {
            fingerTableUpdateTimer.setDelay( delay );
            fingerTableUpdateTimer.setInitialDelay( delay );
        }
        if ( shouldRun && !fingerTableUpdateTimer.isRunning() ) {
            fingerTableUpdateTimer.start();
        } else if ( !shouldRun && fingerTableUpdateTimer.isRunning() ) {
            fingerTableUpdateTimer.stop();
        }
    }


    private void updateFingers( PeerNode node ) {
        try {
            int from = Integer.parseInt( textFingersStart.getText() );
            int to = Integer.parseInt( textFingersEnd.getText() );

            FingerTableUpdateAction.perform( node, from, to );
        } catch ( Throwable t ) {
            log.log( Level.SEVERE, "", t );
        }
    }
    
    private void allNodesUpdateFingers() {
        try {
            // Set the range of finger table rows to be updated to all
            int from = 0;
            int to = network.getNumberOfBits() - 1;
            int nodeCount = peersModel.getSize();
            
            for (int i = 0; i < nodeCount; i++) {
                Object node = peersModel.getElementAt(i);
                
                if (node instanceof PeerNode) {
                    FingerTableUpdateAction.perform((PeerNode)node, from, to);
                }
            }
            
        } catch ( Throwable t ) {
            log.log( Level.SEVERE, "", t );
        }
    }
    
    void updateLog( PeerNode node ) {
        StringBuilder s = new StringBuilder();
        if ( node != null ) {
            // node view
            s.append( "NODE " );
            s.append( node.toString() );
            PeerNode p = node.getChordPredecessor();
            if ( p != null ) {
                s.append( "\npredecessor: " + p + "\n" );
            }
            String fingers = node.dumpChordFingerTable();
            if ( fingers != null ) {
                s.append( fingers );
            }
            s.append( "DATA\n" );
            for ( Map.Entry<String, String> entry : node.getLocalData().entrySet() ) {
                s.append( entry.getKey() );
                s.append( ": " );
                s.append( entry.getValue() );
                s.append( "\n" );
            }
        } else {
            s.append( "NETWORK\n" );
            for ( Message message : network.getMessages() ) {
                s.append( message );
                s.append( "\n" );
            }
        }
        textLog.setText( s.toString() );
    }


    private void getData( PeerNode node, String key ) {
        if ( node == null ) {
            node = network.getRandomPeer();
        }

//		node = node.lookupNodeForItem(node, key);
//		String value = node.getDataItem(node, key);
        node = node.lookupNodeForItem( null, key );
        String value = node.getDataItem( null, key );
        if ( value == null ) {
            value = "Key \"" + key + "\" not found.";
        }
        textValue.setText( value );
    }


    private void setData( PeerNode node, String key, String value ) {
        if ( node == null ) {
            node = network.getRandomPeer();
        }

        node = node.lookupNodeForItem( null, key );

        node.setDataItem( null, key, value );
    }
}

