public class StableTreeTester {
    public static void main(String[] args) throws Exception {
	String baseName = args[0];
	int myId = Integer.parseInt(args[1]);
	int numProc = Integer.parseInt(args[2]);
	Linker comm = new Linker(baseName, myId, numProc);
        if (myId==0) {
            StableSpanRoot bot = new StableSpanRoot(comm);
            for (int i = 0; i < numProc; i++)
                if (i != myId)
                    (new ListenerThread(i, bot)).start();
        } else {
            StableSpanNonroot normal = new StableSpanNonroot(comm);
            for (int i = 0; i < numProc; i++)
                if (i != myId)
                    (new ListenerThread(i, normal)).start();
        }
    }
}
