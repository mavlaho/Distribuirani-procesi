import java.util.LinkedList;

public class GammaSynch extends Process implements Synchronizer {
    // Prvo implementiram beta fazu.

    int acksNeeded;
    IntLinkedList unsafeKids = new IntLinkedList();
    IntLinkedList unsafePrefNodes = new IntLinkedList();
    MsgHandler prog;
    private boolean pulseMsg;

    int pulse;
    SpanForest forest;

    // Označava je li beta faza algoritma gotova.
    private boolean betaEnd;

    // k je argument za klasteriranje.
    public GammaSynch(Linker initComm, int k) {
        super(initComm);
        forest = new SpanForest(comm, k);
        pulse = 0;
    }

    public void initialize(MsgHandler initProg) {
        prog = initProg;
        pulse = 0;
        acksNeeded = 0;
        pulseMsg = false;
        betaEnd = false;
        unsafeKids.addAll(forest.children);
        unsafePrefNodes.addAll(forest.prefEdge);
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("synchAck")) {
            acksNeeded--;
            if (acksNeeded == 0) notify();
        } else if (tag.equals("subtreeSafe")) {
            unsafeKids.remove(src);
            if (unsafeKids.isEmpty()) {
                if (forest.parent == -1)
                    sendChildrenClusterSafe();
                else
                    sendMsg(forest.parent, "subTreeSafe", pulse);
            }
        } else if (tag.equals("clusterSafe")) {
            sendChildrenClusterSafe();
        } else if (tag.equals("ourClusterSafe")) {
            unsafePrefNodes.remove(src);
            trySendParentClustersSafe();
        } else if (tag.equals("neighbouringClusterSafe")) {
            trySendParentClustersSafe();
        } else if (tag.equals("pulse")) {
            sendChildrenPulse();
        } else { // application msg. handle only if pulse number matches
            //else put back in queue
            prog.handleMsg(m, src, tag);
            sendMsg(src, "synchAck", 0);
        }
    }
    private void sendChildrenClusterSafe() {
        betaEnd = true;
        var t = forest.children.listIterator(0);
        while (t.hasNext()) {
            Integer child = (Integer) t.next();
            sendMsg(child.intValue(), "clusterSafe", pulse);
        }

        t = forest.prefEdge.listIterator(0);
        while (t.hasNext()) {
            Integer prefNode = (Integer) t.next();
            sendMsg(prefNode.intValue(), "ourClusterSafe", pulse);
        }

        notify();
        // Za alfa fazu algoritma.
        unsafeKids.addAll(forest.children);
        trySendParentClustersSafe();
    }

    private void sendChildrenPulse() {
        pulseMsg = true;
        var t = forest.children.listIterator(0);
        while (t.hasNext()) {
            Integer child = (Integer) t.next();
            sendMsg(child.intValue(), "pulse", pulse);
        }
        notify();
    }

    // Provjeri je su li uvjeti za slanje ncs
    // poruke zadovoljeni, te je šalje ako jesu.
    private void trySendParentClustersSafe() {
        if (unsafeKids.isEmpty() && unsafePrefNodes.isEmpty()) {
            if (forest.parent == myId) {
                sendChildrenPulse();
            } else {
                sendMsg(forest.parent, "neighbouringClusterSafe", pulse);
            }
            notify();
        }
    }

    public void sendMessage(int destId, String tag, int msg) {
        acksNeeded++;
        sendMsg(destId, tag, msg);
    }
    public void nextPulse() {
        while (acksNeeded != 0) myWait();
        while (forest.children.isEmpty())
            sendMsg(forest.parent, "subtreeSafe", pulse);
        while (!betaEnd) myWait();
        while (!pulseMsg) myWait();
        pulse++; //the node moves to next pulse
       // initialize();
    }
}