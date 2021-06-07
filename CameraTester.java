import java.util.Random;
public class CameraTester {
    public static void main(String[] args) throws Exception {
	String baseName = args[0];
	int myId = Integer.parseInt(args[1]);
	int numProc = Integer.parseInt(args[2]);
        Camera camera = null;
        CamCircToken sp = null;
        if (args[3].equals("RecvCamera")) {
	    Linker comm = new Linker(baseName, myId, numProc);
            sp = new CamCircToken(comm, 0);
            camera = new RecvCamera(comm, sp);
        }
        if (args[3].equals("SenderCamera")) {
	    CameraLinker comm = new CameraLinker(args[0], myId, numProc);
            sp = new CamCircToken(comm, 0);
            camera = new SenderCamera(comm, sp);
        }        
        sp.initiate();       
        for (int i = 0; i < numProc; i++)
            if (i != myId) (new ListenerThread(i, camera)).start();
        if (myId == 0) camera.globalState();
    }
}
