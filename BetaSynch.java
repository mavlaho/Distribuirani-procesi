import java.util.*;
public class BetaSynch extends Process implements Synchronizer {
    int pulse, acksNeeded;
    IntLinkedList unsafeKids = new IntLinkedList();
    MsgHandler prog;
    SpanTree tree;
    private boolean pulseMsg;
    public BetaSynch(Linker initComm, boolean isRoot) {
        super(initComm);
        tree = new SpanTree(comm, isRoot);
        pulse = 0;
    }
    public void initialize(MsgHandler initProg) {
        prog = initProg;
        pulse = 0;
        acksNeeded = 0;
        pulseMsg = false;
        unsafeKids.addAll(tree.children);
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("synchAck")) {
            acksNeeded--;
            if (acksNeeded == 0) notify();
        } else if (tag.equals("subtreeSafe")) {
            unsafeKids.remove(src);
            if (unsafeKids.isEmpty()) {
                if (tree.parent == -1)
                    sendChildrenPulse();
                else
                    sendMsg(tree.parent, "subTreeSafe", pulse);
            }
        } else if (tag.equals("pulse")) {
            sendChildrenPulse();
        } else { // application msg. handle only if pulse number matches
            //else put back in queue
            prog.handleMsg(m, src, tag);
            sendMsg(src, "synchAck", 0);
        }
    }
    private void sendChildrenPulse() {
        pulseMsg = true;
        ListIterator t = tree.children.listIterator(0);
        while (t.hasNext()) {
            Integer child = (Integer) t.next();
            sendMsg(child.intValue(), "pulse", pulse);
        }
        notify();
    }
    public void sendMessage(int destId, String tag, int msg) {
        acksNeeded++;
        sendMsg(destId, tag, msg);
    }
    public void nextPulse() {
        while (acksNeeded != 0) myWait();
        while (tree.children.isEmpty())
            sendMsg(tree.parent, "subtreeSafe", pulse);
        while (!pulseMsg) myWait();
        pulse++; //the node moves to next pulse
       // initialize();
    }
}
