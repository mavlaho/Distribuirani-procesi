import java.util.Timer;
public class StableSpanNonroot extends Process {
    int parent = -1;
    int dist = 0;
    int newDist = 0;//distance after recalculation
    Timer t = new Timer();
    int numReports;
    public StableSpanNonroot(Linker initComm) {
        super(initComm);
        t.schedule(new RestartTask(this), 1000, 1000);
    }
    public synchronized void recalculate(){
	newDist = N;//init newDist to max possible
	sendToNeighbors("Q.dist",0);//query neighbors for their dist
	numReports = comm.neighbors.size();
        while (numReports > 0) myWait(); //wait for all responses
	dist = newDist;
	System.out.println("parent of " + myId + " is " + parent);
	System.out.println("dist of " + myId + " is " + dist);
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("Q.dist")) {
                sendMsg(src, "A.dist", dist); //reply with my dist
        } else if (tag.equals("A.dist")) {
	    int hisDist = m.getMessageInt();
	    if ((hisDist >= 0) && (newDist > hisDist)) {
	       newDist = hisDist+1;
	       parent = src;
	    }
            numReports--;
            notifyAll();
        } else if (tag.equals("restart")) {
            recalculate();
        }
    }
}
