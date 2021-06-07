public class GlobalFuncTester implements FuncUser {
    public int func(int x, int y) {
        return x + y;
    }
    public static void main(String[] args) throws Exception {
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = new Linker(args[0], myId, numProc);
        GlobalFunc g = new GlobalFunc(comm, (myId == 0));
        for (int i = 0; i < numProc; i++)
            if (i != myId) 
                (new ListenerThread(i, g)).start();
        int myValue = Integer.parseInt(args[3]);
        GlobalFuncTester h = new GlobalFuncTester();
        g.initialize(myValue, h);
        int globalSum = g.computeGlobal();
        System.out.println("The global sum is " + globalSum);
    }
}