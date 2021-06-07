import java.util.*;
public class DistSensor extends Process implements Runnable,Sensor {
    final static int red = 0, green = 1;
    int candidate[], color[],G[];
    boolean finished = false, haveToken = false;
    LinkedList q = new LinkedList();
    SensorUser app;
    public DistSensor(VCLinker initComm, SensorUser app) {
        super(initComm); this.app = app;
        candidate = new int[N]; color = new int[N]; G = new int[N];
        for (int j=0; j < N; j++) { color[j] = red; G[j] = 0;}
        if (myId == Symbols.coordinator) haveToken=true;
        new Thread(this).start();
    }
    public synchronized void run(){
        while (!finished) {
            while (!haveToken) myWait();
            handleToken();
        }
    }
    public synchronized void handleToken() {
        while (color[myId] == red) {
            while (q.isEmpty() && !finished) myWait();
            if (q.isEmpty() && finished) {
                app.globalPredicateFalse(myId); return;
            }
            candidate = (int[]) q.removeFirst();
            if (candidate[myId] > G[myId]) {
                G[myId] = candidate[myId]; color[myId] = green;
            }
        }
        for (int j = 0; j < N; j++)
            if ((j != myId) && (candidate[j] >= G[j])) {
                G[j] = candidate[j]; color[j] = red;
            }
        int j = Util.searchArray(color, red);
        if (j != -1) sendToken(j);
        else { app.globalPredicateTrue(G); finished = true; }
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("TokenG")) Util.readArray(m.getMessage(), G);
        else if (tag.equals("Tokencolor")) {
            Util.readArray(m.getMessage(), color);
            haveToken = true;
        } else if (tag.equals("finished")) finished = true;
        notifyAll();
    }
    void sendToken(int j) {
       ((VCLinker) comm).simpleSendMsg(j, "TokenG", Util.writeArray(G));
       ((VCLinker) comm).simpleSendMsg(j,"Tokencolor",Util.writeArray(color));
       haveToken = false;
    }
    public synchronized void localPredicateTrue(VectorClock vc) {
        q.add(vc.v); notifyAll();
    }
}
