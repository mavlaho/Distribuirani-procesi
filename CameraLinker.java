import java.util.*; import java.net.*; import java.io.*;
public class CameraLinker extends Linker {
    static final int white = 0, red = 1;
    int seqNo[] = null;
    SenderCamera cam;
    public CameraLinker(String basename, int myId, int numProc)
                                                throws Exception {
        super(basename, myId, numProc);
        seqNo = new int[numProc];
        for (int i = 0; i < numProc; i++)
            seqNo[i] = 0;
    }
    public void initCam(SenderCamera cam){
        this.cam = cam;
    }
    public void sendMsg(int destId, String tag, String msg) {
        if ((tag.equals("marker")) || (tag.equals("ack")))
            super.sendMsg(destId, tag, msg);
        else {// send seq numbers with app msgs
            seqNo[destId]++;
            Msg m = new  Msg(myId, destId, tag, msg);
            if (cam.myColor == white) {
                cam.recordMsg(destId, new SeqMessage(m, seqNo[destId]));
                super.sendMsg(destId, "white",
                 String.valueOf(seqNo[destId]) +" "+ m.toString()+" ");
            } else
                super.sendMsg(destId, "red", 
                 String.valueOf(seqNo[destId]) +" "+ m.toString()+" ");
        }
    }    
}
