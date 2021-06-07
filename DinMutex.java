public class DinMutex extends Process implements Lock {
    private static final int thinking = 0, hungry = 1, eating = 2;
    boolean fork[] = null,  dirty[] = null, request[] = null;
    int myState = thinking;
    public DinMutex(Linker initComm) {
        super(initComm);
        fork = new boolean[N]; dirty = new boolean[N];
        request = new boolean[N];
        for (int i = 0; i < N; i++) {
            if ((myId > i) && (isNeighbor(i))) {
                fork[i] = false; request[i] = true;
            } else { fork[i] = true; request[i] = false; }
            dirty[i] = true;
        }
    }
    public synchronized void requestCS() {
        myState = hungry;
        if (haveForks()) myState = eating;
        else
            for (int i = 0; i < N; i++)
                if (request[i] && !fork[i]) {
                    sendMsg(i, "Request"); request[i] = false;
                }
        while (myState != eating) myWait();
    }
    public synchronized void releaseCS() {
        myState = thinking;
        for (int i = 0; i < N; i++) {
            dirty[i] = true;
            if (request[i]) { sendMsg(i, "Fork");  fork[i] = false; }
        }
    }
    boolean haveForks() {
        for (int i = 0; i < N; i++)
            if (!fork[i]) return false;
        return true;
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("Request")) {
            request[src] = true;
            if ((myState != eating) && fork[src] && dirty[src]) {
                sendMsg(src, "Fork");  fork[src] = false;
                if (myState == hungry){
                    sendMsg(src, "Request"); request[src] = false;
                }
            }
        } else if (tag.equals("Fork")) {
            fork[src] = true; dirty[src] = false;
            if (haveForks()) {
                myState = eating; notify();
            }
        }
    }
}
