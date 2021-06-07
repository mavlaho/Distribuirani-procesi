public class TwoPhaseCoord extends Process {
    boolean globalCommit = false;
    boolean donePhase1 = false;
    boolean noReceived = false;
    int numParticipants;
    int numReplies = 0;
    public TwoPhaseCoord(Linker initComm) {
        super(initComm);
        numParticipants = N - 1;      
    }
    public synchronized  void doCoordinator() {
        // Phase 1
        broadcastMsg("request", myId);
        while (!donePhase1)
            myWait();

        // Phase 2
        if (noReceived)
            broadcastMsg("finalAbort", myId);
        else {
            globalCommit = true;
            broadcastMsg("finalCommit", myId);
        }
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("yes")) {
            numReplies++;
            if (numReplies == numParticipants) {
                donePhase1 = true;
                notify();
            }
        } else if (tag.equals("no")) {
            noReceived = true;
            donePhase1 = true;
            notify();
        }
    }
}
