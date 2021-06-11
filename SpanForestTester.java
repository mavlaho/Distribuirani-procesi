public class SpanForestTester {
    public static void main(String[] args) throws Exception {
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        int k = Integer.parseInt(args[3]);
        Linker comm = new Linker(args[0], myId, numProc);
        SpanForest t = new SpanForest(comm, k);
        for (int i = 0; i < numProc; i++)
            if (i != myId)
                (new ListenerThread(i, t)).start();
        t.waitForDone();
        Util.println(myId + ":" + t.children.toString());
        Util.println(myId + ":" + t.prefEdge.toString());
    }
}

