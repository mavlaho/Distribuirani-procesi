import java.util.*;
public class KingBGA extends Process {
    final static int defaultValue = 0;
    int f; // maximum number of faults
    int V[]; // set of values known
    int kingValue, myValue;
    public KingBGA(Linker initComm, int f) {
        super(initComm);
        this.f = f;
        V = new int[N];
    }
    public synchronized void propose(int val) {
        for (int i = 0; i < N; i++) V[i] = defaultValue;
        V[myId] = val;
    }
    public int decide() {
        for (int k = 0; k <= f; k++) { // f+1 rounds
            broadcastMsg("phase1", V[myId]);
            Util.mySleep(Symbols.roundTime);
            synchronized (this) {
                myValue = getMajority(V);
                if (k == myId)
                    broadcastMsg("king", myValue);
            }
            Util.mySleep(Symbols.roundTime);
            synchronized (this) {
                if (numCopies(V, myValue) > N / 2 + f)
                    V[myId] = myValue;
                else
                    V[myId] = kingValue;
            }
        }
        return V[myId];
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("phase1")) {
            V[src] = m.getMessageInt();
        } else if (tag.equals("king")) {
            kingValue = m.getMessageInt();
        }
    }
    int getMajority(int V[]) {
        if (numCopies(V, 0) > N / 2)
            return 0;
        else if (numCopies(V, 1) > N / 2)
            return 1;
        else
            return defaultValue;
    }
    int numCopies(int V[], int v) {
        int count = 0;
        for (int i = 0; i < V.length; i++)
            if (V[i] == v) count++;
        return count;
    }
}
