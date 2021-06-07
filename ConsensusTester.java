public class ConsensusTester {
    public static void main(String[] args) throws Exception {
        String baseName = args[0];
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = new Linker(baseName, myId, numProc);
        Consensus sp = new Consensus(comm, 3);
        for (int i = 0; i < numProc; i++)
            if (i != myId) (new ListenerThread(i, sp)).start();
        sp.propose(myId);
        System.out.println("The value decided:" + sp.decide());
    }
}
