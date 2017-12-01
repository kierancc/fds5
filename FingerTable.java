/*
 * Copyright (c) Databases and Information Systems Research Group, University of Basel, Switzerland
 */

package ch.unibas.dmi.dbis.fds.p2p;


import java.util.ArrayList;
import java.util.List;


public class FingerTable<T extends ChordPeerNode> {

    private final T owner;


    public class Entry {

        /**
         * The start, inclusive.
         */
        private final long start;

        /**
         * The end, exclusive.
         */
        private final long end;

        private T node;


        public long getStart() {
            return start;
        }


        public long getEnd() {
            return end;
        }


        public T getNode() {
            return node;
        }


        public void setNode( T node ) {
            if ( this.node != null ) {
                owner.removeConnection( this.node.nodeID );
            }
            this.node = node;

            System.out.println( "FingerTable: finger changed at " + owner.n + ": " + toString() );
            owner.addConnection( node.nodeID );
        }


        public Entry( long n, int m, int k ) {
            this.start = (n + (1 << k)) % (1 << m);
            this.end = (n + (1 << (k + 1))) % (1 << m);
        }


        @Override
        public String toString() {
            return "[" + start + "," + end + ") : " + node;
        }
    }


    private final List<Entry> entries;


    public Entry get( int index ) {
        return entries.get( index );
    }


    public int size() {
        return entries.size();
    }


    public FingerTable( T owner, int m ) {
        this.owner = owner;
        entries = new ArrayList<Entry>( m );
        for ( int k = 0; k < m; ++k ) {
            entries.add( new Entry( owner.n, m, k ) );
        }
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for ( int i = 0; i < entries.size(); ++i ) {
            s.append( "finger " + i + ": " );
            s.append( entries.get( i ).toString() );
            s.append( "\n" );
        }

        return s.toString();
    }
}
