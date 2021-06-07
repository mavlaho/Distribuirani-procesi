import java.util.Random;
public class TermTester {
    public static void main(String[] args) throws Exception {
	String baseName = args[0];
	int myId = Integer.parseInt(args[1]);
	int numProc = Integer.parseInt(args[2]);
	Linker comm = new Linker(baseName, myId, numProc);
        int[] edgeCost = new int[numProc];
        Random r = new Random();
        for (int i = 0; i < numProc; i++)
            if (i != myId) edgeCost[i] = r.nextInt(1000);
        edgeCost[myId] = 0;
        for (int i = 0; i < numProc; i++)
            System.out.println("weight of(" + i + "," + myId + ")=" + edgeCost[i]);
        int alg = Integer.parseInt(args[3]);      
        ShortestPath sp = null;
        if (alg == 0)
            sp = new ShortestPath(comm, edgeCost);
        else if (alg == 1)
            sp = new TermShortestPath(comm, edgeCost,  new
                    DSTerm(comm));
        else if (alg == 2)
            sp = new TermShortestPath(comm, edgeCost, new
                    TermToken(comm));
        for (int i = 0; i < numProc; i++)
            if (i != myId) (new ListenerThread(i, sp)).start();
        sp.initiate(); // start the computation
    }
}
