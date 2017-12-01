package ch.unibas.dmi.dbis.fds.p2p;

import java.util.TimerTask;

public class StabilizeTimerTask extends TimerTask
{
    private final ChordPeerImpl node;
    
    public StabilizeTimerTask(ChordPeerImpl node_)
    {
        this.node = node_;
    }
    
    @Override
    public void run()
    {
        this.node.stabilize(this.node);
        
    }

}
