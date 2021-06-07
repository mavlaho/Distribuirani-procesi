import java.util.*;
public class GlobalFunc extends Process implements GlobalService {
    FuncUser prog;
    SpanTree tree = null;
    IntLinkedList pending = new IntLinkedList();
    int myValue;
    int answer;
    boolean answerRecvd;
    boolean pendingSet = false;
    public GlobalFunc(Linker initComm, boolean isRoot) {
        super(initComm);
        tree = new SpanTree(comm, isRoot);
    }
    public void initialize(int myValue, FuncUser prog) {
        this.myValue = myValue;
        this.prog = prog;
        tree.waitForDone();
        Util.println(myId + ":" + tree.children.toString());
    }
    public synchronized int computeGlobal() {
        pending.addAll(tree.children);
        pendingSet = true;
        notifyAll();
        while (!pending.isEmpty()) myWait();
        if (tree.parent == myId) { // root node
            answer = myValue;
        } else { //non-root node
            sendMsg(tree.parent, "subTreeVal", myValue);
            answerRecvd = false;
            while (!answerRecvd) myWait();
        }
        sendChildren(answer);
        return answer;
    }
    void sendChildren(int value) {
        ListIterator t = tree.children.listIterator(0);
        while (t.hasNext()) {
            Integer child = (Integer) t.next();
            sendMsg(child.intValue(), "globalFunc", value);
        }
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        tree.handleMsg(m, src, tag);
        if (tag.equals("subTreeVal")) {
            while (!pendingSet) myWait();
            pending.remove(new Integer(src));
            myValue = prog.func(myValue, m.getMessageInt());
            if (pending.isEmpty()) notifyAll();
        } else if (tag.equals("globalFunc")) {
            answer = m.getMessageInt();
            answerRecvd = true;
            notifyAll();
        }
    }
}

