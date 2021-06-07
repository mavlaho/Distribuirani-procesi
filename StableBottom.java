import java.util.Timer;
public class StableBottom extends Process implements Lock {
    int myState = 0;
    int leftState = 0;
    int next;
    Timer t = new Timer();
    boolean tokenSent = false;
    public StableBottom(Linker initComm) {
        super(initComm);
	next = (myId + 1) % N;
    }
    public synchronized void initiate() {
        t.schedule(new RestartTask(this), 1000, 1000);
    }
    public synchronized void requestCS() {
        while (leftState != myState) myWait();
    }
    public synchronized void releaseCS() {
        myState = (leftState + 1) % N;
    }
    public synchronized void sendToken() {
        if (!tokenSent) {
          sendMsg(next, "token", myState);
	  tokenSent = true;
	} else tokenSent = false;
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("token") )
        {
            leftState = m.getMessageInt();
            notify();
	    Util.mySleep(1000);
            sendMsg(next, "token", myState);
	    tokenSent = true;
        } else if (tag.equals("restart") )
            sendToken();
    }
}
