import java.util.*;
public class RecvCamera  extends Process implements Camera {
    static final int white = 0, red = 1;
    int myColor = white;
    boolean closed[];
    CamUser app;
    LinkedList chan[] = null;
    public RecvCamera(Linker initComm, CamUser app) {
        super(initComm);
        closed = new boolean[N];
        chan = new LinkedList[N];
        for (int i = 0; i < N; i++)
            if (isNeighbor(i)) {
                closed[i] = false;
                chan[i] = new LinkedList();
            } else closed[i] = true;
        this.app = app;
    }
    public synchronized void globalState() {
        myColor = red;
        app.localState(); // record local State;
        sendToNeighbors("marker", myId);  // send Markers
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("marker")) {
            if (myColor == white) globalState();
            closed[src] = true;
            if (isDone()){
                System.out.println("Channel State: Transit Messages ");
                for (int i = 0; i < N; i++)
                    if (isNeighbor(i))
                        while (!chan[i].isEmpty())
                            System.out.println(
                            ((Msg) chan[i].removeFirst()).toString());
            }
        } else { // application message
            if ((myColor == red) && (!closed[src]))
                chan[src].add(m);
            app.handleMsg(m, src, tag); // give it to app
        }
    }
    boolean isDone() {
        if (myColor == white) return false;
        for (int i = 0; i < N; i++)
            if (!closed[i]) return false;
        return true;
    }
}
