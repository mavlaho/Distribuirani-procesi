public class SensorTester {
    public static void main(String[] args) throws Exception {
	String baseName = args[0];
	int myId = Integer.parseInt(args[1]);
	int numProc = Integer.parseInt(args[2]);
	VCLinker comm = new VCLinker(baseName, myId, numProc);
        int algoCode = Integer.parseInt(args[3]);       
        SensorCircToken sp = new SensorCircToken(
                     comm, Symbols.coordinator, algoCode);
        sp.initiate();
        for (int i = 0; i < numProc; i++)
            if (i != myId) (new ListenerThread(i, sp)).start();
    }
}
