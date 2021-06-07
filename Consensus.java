import java.util.*;
public class Consensus extends Process {
    int myValue;
    int f; // maximum number of faults
    boolean changed = true;
    boolean hasProposed = false;
    public Consensus(Linker initComm, int f) {
        super(initComm);
        this.f = f;
    }
    public synchronized void propose(int value) {
        myValue = value;
        hasProposed = true;
        notify();
    }
    public int decide() {
        for (int k = 0; k <= f; k++) { // f+1 rounds
            synchronized (this) {
                if (changed) broadcastMsg("proposal", myValue);
            }
            // sleep enough to receive messages for this round
            Util.mySleep(Symbols.roundTime);
        }
        synchronized (this) {
            return myValue;
        }
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        while (!hasProposed) myWait();
        if (tag.equals("proposal")) {
            int value = m.getMessageInt();
            if (value < myValue) {
                myValue = value;
                changed = true;
            } else
                changed = false;
        }
    }
}
