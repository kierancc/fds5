/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Abstract class for network simulation This class is a starting point for implementation of different p2p networks.
 *
 * @author Gert Brettlecker
 * @author Christoph Langguth
 */
public abstract class Network {

    public static Network newFullyConnectedNetwork( final int numberOfBits ) {
        return new FullyConnectedNetwork( numberOfBits );
    }


    public static Network newChordNetwork( final int numberOfBits, Class<? extends ChordPeerNode> chordPeerClassName ) {
        return new ChordNetwork( numberOfBits, chordPeerClassName );
    }


    private class GuiPanel extends javax.swing.JPanel {

        private static final long serialVersionUID = 1L;


        @Override
        public Dimension getPreferredSize() {
            return new Dimension( 500, 550 );
        }


        /**
         * This method draws the network.
         *
         * @param g the graphic device to be used
         */
        @Override
        public void paint( Graphics g ) {
            Graphics2D g2d = (Graphics2D) g;

            // no nodes nothing to draw!
            if ( nodes.size() == 0 ) {
                return;
            }

            BasicStroke nodeStroke = new BasicStroke( 2.0f );

            BasicStroke connectionStroke = new BasicStroke( 1,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1,
                    new float[]{ 2, 6 }, 0 );

            BasicStroke messageStroke = new BasicStroke( 1,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1,
                    new float[]{ 10, 20 }, 0 );

            ArrayList<Long> duplicateHashCheck = new ArrayList<Long>();

            g2d.setColor( Color.YELLOW );
            g2d.drawOval( 20, 20, 400, 400 );

            long numberOfConnections = 0;

            // Draw client node
            g2d.setColor( Color.RED );
            g2d.setStroke( nodeStroke );
            g2d.drawRect( 5, 5, 10, 10 );

            synchronized ( nodes ) {
                for ( PeerNode p : nodes.values() ) {

                    // Duplicate check
                    long nodeHash = hash( p.getNodeID() );
                    if ( !duplicateHashCheck.contains( nodeHash ) ) {
                        duplicateHashCheck.add( nodeHash );
                    } else {
                        System.err.println( "Node hash duplicate for "
                                + p.getNodeID() + " !" );
                    }

                    // Draw peer nodes
                    double alpha = getAngleForNode( p.getNodeID() );
                    g2d.setColor( Color.BLUE );
                    g2d.setStroke( nodeStroke );
                    g2d.drawRect( (int) (220 - 5 + 200 * Math.sin( alpha )),
                            (int) (220 - 5 + 200 * Math.cos( alpha )), 10, 10 );
                    g2d.drawString( String.valueOf( nodeHash ),
                            ((int) (220 - 5 + 200 * Math.sin( alpha ))) + ((int) (2 + 14 * Math.sin( alpha ))),
                            (int) (230 - 5 + 200 * Math.cos( alpha )) + (int) (18 * Math.cos( alpha )) );

                    // Draw connection lines
                    for ( String toID : p.getConnections() ) {
                        double alpha2 = getAngleForNode( toID );
                        g2d.setColor( Color.GRAY );
                        g2d.setStroke( connectionStroke );
                        numberOfConnections++;
                        g2d.drawLine( (int) (220 + 200 * Math.sin( alpha )),
                                (int) (220 + 200 * Math.cos( alpha )),
                                (int) (220 + 200 * Math.sin( alpha2 )),
                                (int) (220 + 200 * Math.cos( alpha2 )) );
                    }
                }
            }

            synchronized ( passedMessages ) {
                int numberOfMessages = passedMessages.size();

                // Draw message lines
                for ( Message m : passedMessages ) {
                    double alpha1, alpha2;
                    int x1 = 10, y1 = 10, x2 = 10, y2 = 10;
                    if ( m.getSourceNodeId() != null ) {
                        alpha1 = getAngleForNode( m.getSourceNodeId() );
                        x1 = (int) (220 + 200 * Math.sin( alpha1 ));
                        y1 = (int) (220 + 200 * Math.cos( alpha1 ));
                    }
                    if ( m.getDestinationNodeId() != null ) {
                        alpha2 = getAngleForNode( m.getDestinationNodeId() );
                        x2 = (int) (220 + 200 * Math.sin( alpha2 ));
                        y2 = (int) (220 + 200 * Math.cos( alpha2 ));
                    }
                    switch ( m.getMsgType() ) {
                        case GET:
                            g2d.setColor( Color.GREEN );
                            break;
                        case GET_RESPONSE:
                            g2d.setColor( Color.GREEN );
                            break;
                        case SET:
                            g2d.setColor( Color.ORANGE );
                            break;
                        case SET_RESPONSE:
                            g2d.setColor( Color.ORANGE );
                            break;
                    }
                    g2d.setStroke( messageStroke );
                    g2d.drawLine( x1, y1, x2, y2 );
                }

                int numberOfLookupQueries = getNumberOfMessages( Message.MessageType.LOOKUP );
                int numberOfGetQueries = getNumberOfMessages( Message.MessageType.GET );
                int numberOfSaveQueries = getNumberOfMessages( Message.MessageType.SET );

                g2d.setColor( Color.BLACK );
                g2d.drawString( "Number of peers: " + nodes.size(), 20, 450 );

                g2d.drawString( "Number of connections: " + numberOfConnections
                                + " per Peer: " + (numberOfConnections / nodes.size()), 20,
                        470 );

                g2d.drawString( "Number of lookup/get/save queries: " + numberOfLookupQueries + "/" + numberOfGetQueries
                        + "/" + numberOfSaveQueries, 20, 490 );

                g2d.drawString( "Number of messages: " + numberOfMessages, 20, 510 );
            }
        }


        /**
         * Helping method for paintNetwork to retrieve the angle of a peer at the illustration circle
         *
         * @param nodeId of the peer to be drawn
         * @return angle of peer on circle
         */
        private double getAngleForNode( String nodeId ) {
            return (double) hash( nodeId ) / Math.pow( 2, numberOfBits ) * 2
                    * Math.PI;
        }
    }


    private final GuiPanel panel = new GuiPanel();


    GuiPanel getPanel() {
        return panel;
    }


    /**
     * Number of bits used for hashing node ids and data keys.
     */
    private final int numberOfBits;

    /**
     * Map containing all nodes within the network Key: node id
     */
    protected final Map<String, PeerNode> nodes = new ConcurrentHashMap<String, PeerNode>();

    /**
     * Statistics: List for storing passed messages.
     */
    private final List<Message> passedMessages = new ArrayList<Message>();


    /**
     * Constructor
     *
     * @param numberOfBits used for the hashing of keys. Must be between 2 and 56 (inclusive)
     */
    protected Network( int numberOfBits ) {
        if ( (numberOfBits < 2) || (numberOfBits > 56) ) {
            throw new RuntimeException( "Number of bits: " + numberOfBits
                    + " not supported!" );
        }
        this.numberOfBits = numberOfBits;
    }


    /**
     * Allows to join a node to the network.
     *
     * @param node to be joined
     */
    void addPeer( PeerNode node ) {
        synchronized ( nodes ) {
            nodes.put( node.getNodeID(), node );
        }
    }


    /**
     * Allows to retrieve a node from the network.
     *
     * @param nodeId id to be retrieved
     * @return peer node object
     */
    public PeerNode getPeer( String nodeId ) {
        synchronized ( nodes ) {
            return nodes.get( nodeId );
        }
    }


    /**
     * Allows to retrieve a random node from the network.
     *
     * @return peer node object
     */
    public PeerNode getRandomPeer() {
        synchronized ( nodes ) {
            if ( nodes.size() == 0 ) {
                return null;
            }
            int i = (int) (Math.random() * nodes.size());
            return nodes.values().toArray( new PeerNode[nodes.size()] )[i];
        }
    }


    /**
     * Implement this in subclass to arrange the P2P overlay structure.
     */
    abstract void arrangeOverlayStructure();


    /**
     * Clear logging of messages and queries.
     */
    public void clearLogs() {
        synchronized ( passedMessages ) {
            passedMessages.clear();
        }
    }


    /**
     * Used to log passed messages for statistics. Messages are NOT logged if from and to are identical (i.e., local call).
     *
     * @param msgType message type
     * @param fromPeer sender peer of message (client app if null)
     * @param toPeer receiver peer of message (client app if null)
     */
    protected void logPassedMessage( Message.MessageType msgType, PeerNode fromPeer, PeerNode toPeer ) {
        String fromID = null;
        String toID = null;
        if ( fromPeer != null ) {
            fromID = fromPeer.getNodeID();
        }
        if ( toPeer != null ) {
            toID = toPeer.getNodeID();
        }
        // ignore local calls
        if ( fromPeer != null && fromPeer.equals( toPeer ) ) {
            // skip
        } else {
            Message msg = new Message( msgType, fromID, toID );
            synchronized ( passedMessages ) {
                passedMessages.add( msg );
            }
        }
    }


    /**
     * Helping method to return the number of passed messages of a given message type.
     *
     * @param msgType the message type to be counted
     * @return number of messages passed
     */
    private int getNumberOfMessages( Message.MessageType msgType ) {
        int ret = 0;
        synchronized ( passedMessages ) {
            for ( Message m : passedMessages ) {
                if ( m.getMsgType().equals( msgType ) ) {
                    ret++;
                }
            }
        }
        return ret;
    }


    public List<Message> getMessages() {
        synchronized ( passedMessages ) {
            return new ArrayList<Message>( passedMessages );
        }
    }


    /**
     * This function generates a hash function based on the numberOfBits
     *
     * @param value the string to be hashed
     * @return long hash value;
     */
    public final long hash( String value ) {
        try {
            int basis = (int) Math.pow( 2, this.numberOfBits );
            MessageDigest md = MessageDigest.getInstance( "SHA" );
            byte[] digest = md.digest( value.getBytes() );
            long v = 0;
            for ( int i = 0; i < 7; i++ ) {
                v = v + (digest[i] & 0xff) * (int) Math.pow( 256, i );
            }
            v = v % basis;
            return v;
        } catch ( NoSuchAlgorithmException e ) {
            System.err.println( "Hash not supported by your JVM!" );
            return -1;
        }
    }


    /**
     * This method allows to check if a given hashID is within a sector of a chord identifier ring. Attention "within" means that if hashID is equal to start or end it is also within.
     *
     * @param hashID the given hash identifier to check
     * @param startOfSectorHashID the hash identifier of start of sector
     * @param endOfSectorHashID the hash identifier of end of sector
     * @return true if hashID is within the sector otherwise false
     */
    private static boolean isHashInRingSector( long hashID, long startOfSectorHashID, long endOfSectorHashID ) {

        // Check if 0 is within the given sector
        boolean crossingZero = false;
        if ( startOfSectorHashID > endOfSectorHashID ) {
            crossingZero = true;
        }

        // Comparison to check whether hashID is in the sector
        if ( !crossingZero ) {
            // 0 is not within the sector (the easy case :-) )
            // in this case if hashID is bigger than end or smaller than start
            // => we know hashID is outside
            if ( (hashID > endOfSectorHashID) || (hashID < startOfSectorHashID) ) {
                return false;
            } else {
                return true;
            }
        } else {
            // 0 is within the sector (the complex case :-( )
            // in this case if hashID is greater/equal than start or
            // smaller/equal than end => we know hashID is inside
            if ( (hashID >= startOfSectorHashID)
                    || (hashID <= endOfSectorHashID) ) {
                return true;
            } else {
                return false;
            }
        }

    }


    /**
     * Checks if a hash value is an element of a given sector on the chord ring.
     *
     * @param hash the hash to check for inclusion in the sector
     * @param start the start value of the sector
     * @param end the end the end value of the sector
     * @param startInclusive set to <tt>true</tt> if the start value should be included (closed interval: "["); set to <tt>false</tt> if the start value should be excluded (open interval: "(").
     * @param endInclusive set to <tt>true</tt> if the end value should be included (closed interval: "]"); set to <tt>false</tt> if the end value should be excluded (open interval: ")").
     * @return true, if is hash element of the given ring sector
     */
    public boolean isHashElementOf( long hash, long start, long end, boolean startInclusive, boolean endInclusive ) {
        //System.out.print(hash+" e " + (startInclusive ? "[":"(") + start + "," + end + (endInclusive ? "]":")") +" : ");
        if ( !startInclusive && !endInclusive ) {
            // special case: open interval between (i, i+1) is always empty, but the following
            // calculations would screw up.
            // the same is true for (2^m-1, 0)
            if ( start == end - 1 ) {
                return false;
            }
            if ( start == (1 << numberOfBits) - 1 && end == 0 ) {
                return false;
            }
        }
        if ( !startInclusive ) {
            start++;
            if ( start == (1 << numberOfBits) ) {
                start = 0;
            }
        }
        if ( !endInclusive ) {
            end--;
            if ( end == -1 ) {
                end += (1 << numberOfBits);
            }
        }
        boolean ret = isHashInRingSector( hash, start, end );
        return ret;
    }


    public int getNumberOfBits() {
        return numberOfBits;
    }


    public abstract PeerNode createPeer( String id, boolean useSuccessorsOnly );
}
