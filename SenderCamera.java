import java.util.*;
public class SenderCamera extends Process implements Camera {
    static final int white = 0, red = 1;
    public int myColor = white;
    CamUser app;
    boolean closed[];
    MsgList outChan[] = null;
    public SenderCamera(CameraLinker initComm, CamUser app) {
        super(initComm);
        this.app = app;
        closed = new boolean[N]; outChan = new MsgList[N];
        for (int i = 0; i < N; i++)
            if (isNeighbor(i)) {
                closed[i] = false;
                outChan[i] = new MsgList();
            } else closed[i] = true;
        initComm.initCam(this);
    }
    public synchronized void globalState() {
        myColor = red;
        app.localState(); // record local State;
        sendToNeighbors("marker", myId);  //  send Markers
    }
    public synchronized void handleMsg(Msg m, int src, String tag){
        if (tag.equals("marker")) {
            if (myColor == white) globalState();
            closed[src] = true;
            if (isDone()) System.out.println("Done recording");
        } else if (tag.equals("ack")) {
            int seqNo = m.getMessageInt();
            outChan[src].removeM(seqNo);
        } else { // application message
            StringTokenizer st=new StringTokenizer(m.getMessage()+"#");
            int seqNo = Integer.parseInt(st.nextToken());
            Msg appMsg = Msg.parseMsg(st);
            if ((myColor == white) && (tag.equals("white")))
                sendMsg(src, "ack", seqNo);
            if ((myColor == white) && (tag.equals("red")))
                globalState();
            app.handleMsg(appMsg, src, appMsg.getTag());
        }
    }
    boolean isDone() {
        if (myColor == white) return false;
        for (int i = 0; i < N; i++)
            if (!closed[i]) return false;
        return true;
    }
    public synchronized void recordMsg(int destId, SeqMessage sm){
        outChan[destId].add(sm);
    }
}
