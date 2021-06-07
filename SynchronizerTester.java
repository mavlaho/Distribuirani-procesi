import java.util.Random;
public class SynchronizerTester {
    public static void main(String[] args) throws Exception {
        String baseName = args[0];
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = new Linker(baseName, myId, numProc);
        Synchronizer pulser = null;
        if (args[3].equals("simple"))
            pulser = new SimpleSynch(comm);
        else if (args[3].equals("alpha"))
            pulser = new AlphaSynch(comm);
        else if (args[3].equals("beta"))
            pulser = new BetaSynch(comm, (myId == 0) );
        SynchBfsTree  app = new SynchBfsTree(comm, pulser, (myId == 0));        
        for (int i = 0; i < numProc; i++)
            if (i != myId) (new ListenerThread(i, pulser)).start();
        app.initiate();
    }
}
