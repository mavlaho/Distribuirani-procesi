import java.util.*;
public class CentSensor extends Process implements Runnable, Sensor {
    final static int red = 0, green = 1;
    LinkedList q[]; // q[i] stores vector timestamps from process i
    int cut[][], color[],gstate[];
    boolean finished[]; // process i finished
    SensorUser app; final int checker = Symbols.coordinator;
    public CentSensor(VCLinker initComm, SensorUser app) {
        super(initComm);
        cut = new int[N][N]; q = new LinkedList[N];
        color = new int[N]; gstate = new int[N];finished = new boolean[N];
        for (int i = 0; i < N; i++) {
            q[i] = new LinkedList(); color[i] = red; finished[i] = false;
        }
        this.app = app;
        if (myId == checker) new Thread(this).start();
    }
    public synchronized void localPredicateTrue(VectorClock vc){
        if (myId == checker)
          handleMsg(new Msg(0,0,"trueVC", vc.toString()),0,"trueVC");
        else
          ((VCLinker)comm).simpleSendMsg(checker,"trueVC",vc.toString());
    }
    public synchronized void run() {
        int i = Util.searchArray(color, red);
        while (i != -1) {
            while (q[i].isEmpty() && !finished[i]) myWait();
            if (finished[i]) {
                app.globalPredicateFalse(i);
                return;
            }
            cut[i] = (int[]) q[i].removeFirst();
            paintState(i);
            i = Util.searchArray(color, red);
        }
        for (int j = 0; j < N; j++) gstate[j] = cut[j][j];
        app.globalPredicateTrue(gstate);
    }
    public synchronized void handleMsg(Msg m, int src, String tag){
        if (tag.equals("trueVC")) {
            int[] receiveTag = new int[N];
            Util.readArray(m.getMessage(), receiveTag);
            q[src].add(receiveTag); notify();
        } else if (tag.equals("finished")) {
            finished[src] = true; notify();
        }
    }
    void paintState(int i) {
        color[i] = green;
        for (int j = 0; j < N; j++)
            if (color[j] == green)
                if (Util.lessThan(cut[i], cut[j])) color[i] = red;
                else if (Util.lessThan(cut[j], cut[i])) color[j] = red;
    }
}
