public class TwoPhaseTester {
    public static void main(String[] args) throws Exception {
        String baseName = args[0];
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = new Linker(baseName, myId, numProc);
        if (myId == 0) {
            TwoPhaseCoord master = new TwoPhaseCoord(comm);
            for (int i = 0; i < numProc; i++)
                if (i != myId)
                    (new ListenerThread(i, master)).start();
            master.doCoordinator();
        }
        else {
            TwoPhaseParticipant slave = new TwoPhaseParticipant(comm);
            for (int i = 0; i < numProc; i++)
                if (i != myId)
                    (new ListenerThread(i, slave)).start();           
            if (args[3].equals("t")) slave.propose(true);
            else slave.propose(false);
            System.out.println("The value decided:" + slave.decide());
        }
    }
}