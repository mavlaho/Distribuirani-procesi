import java.util.Timer;
public class StableSpanRoot extends Process {
    int parent = -1;
    int dist = 0;
    Timer t = new Timer();
    public StableSpanRoot(Linker initComm) {
        super(initComm);
        t.schedule(new RestartTask(this), 1000, 1000);
    }
    public synchronized void recalculate(){
        parent = -1;
        dist = 0;
	System.out.println("parent of " + myId + " is " + parent);
	System.out.println("dist of " + myId + " is " + dist);
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("Q.dist")) {
                sendMsg(src, "A.dist", 0);
        } else if (tag.equals("restart")) {
            recalculate();
        }
    }
}
