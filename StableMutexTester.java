public class StableMutexTester {
    public static void main(String[] args) throws Exception {
	String baseName = args[0];
	int myId = Integer.parseInt(args[1]);
	int numProc = Integer.parseInt(args[2]);
	Linker comm = new Linker(baseName, myId, numProc);
        if (myId == 0) {
            StableBottom bot = new StableBottom(comm);
            for (int i = 0; i < numProc; i++)
                if (i != myId)
                    (new ListenerThread(i, bot)).start();
            bot.initiate();
            while (true) {
                System.out.println(myId + " is not in CS");
                bot.requestCS();
                System.out.println(myId + " is in CS *****");
                bot.releaseCS();
            }
        } else {
            StableNormal normal = new StableNormal(comm);
            for (int i = 0; i < numProc; i++)
                if (i != myId)
                    (new ListenerThread(i, normal)).start();
            while (true) {
                System.out.println(myId + " is not in CS");
                normal.requestCS();
                System.out.println(myId + " is in CS *****");
                normal.releaseCS();
            }
        }
    }
}
