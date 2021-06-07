import java.util.LinkedList;
public class AlphaSynch extends Process implements Synchronizer {
    int pulse = -1;
    int acksNeeded = 0;
    IntLinkedList unsafe = new IntLinkedList();
    LinkedList nextPulseMsgs = new LinkedList();//msgs for next pulse
    boolean meSafe;
    MsgHandler prog;
    public AlphaSynch(Linker initComm) {
        super(initComm);
    }
    public synchronized void initialize(MsgHandler initProg) {
        prog = initProg;
        startPulse();
        notifyAll();
    }
    void startPulse(){
        unsafe.addAll(comm.neighbors);
        meSafe = false;
        pulse ++;
        Util.println("**** new pulse ****:" + pulse);
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        while (pulse < 0) myWait();
        if (tag.equals("synchAck")) {
            acksNeeded--;
            if (acksNeeded == 0) notifyAll();
        } else if (tag.equals("safe")) {
            while (!unsafe.contains(src)) myWait();
            unsafe.removeObject(src);
            if (unsafe.isEmpty()) notifyAll();
        } else { // application message
            sendMsg(src, "synchAck", 0);
            while (!unsafe.contains(src)) myWait();            
            if (meSafe) nextPulseMsgs.add(m);
            else prog.handleMsg(m, src, tag);
        }
    }
    public synchronized void sendMessage(int destId, String tag, int msg) {
        acksNeeded++;
        sendMsg(destId, tag, msg);
    }
    public synchronized void nextPulse() {      
        while (acksNeeded != 0) myWait();
        meSafe = true;
        sendToNeighbors("safe", 0);     
        while (!unsafe.isEmpty()) myWait();
        startPulse();
        while (!nextPulseMsgs.isEmpty()) {//act on msgs received earlier
            Msg m = (Msg) nextPulseMsgs.removeFirst();
            prog.handleMsg(m, m.getSrcId(), m.getTag());
        }
        notifyAll();
    }
}
