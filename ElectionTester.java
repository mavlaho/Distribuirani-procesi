public class ElectionTester {
    public static void main(String[] args) throws Exception {
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = new Linker(args[0], myId, numProc);
        Election  g = new RingLeader(comm, myId);
        for (int i = 0; i < numProc; i++)
            if (i != myId)
                (new ListenerThread(i, g)).start();
	g.startElection();
        int leader = g.getLeader();
        System.out.println("The leader is " + leader);
    }
}

