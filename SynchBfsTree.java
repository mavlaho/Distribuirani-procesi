public class SynchBfsTree extends Process {
    int parent = -1;
    int level;
    Synchronizer s;
    boolean isRoot;
    public SynchBfsTree(Linker initComm, 
                        Synchronizer initS, boolean isRoot) {
        super(initComm);
        s = initS;
        this.isRoot = isRoot;
    }
    public void initiate() {
        if (isRoot) {
            parent = myId;
            level = 0;
        }
        s.initialize(this);
        for (int pulse = 0; pulse < N; pulse++) {
            if ((pulse == 0) && isRoot) {
                for (int i = 0; i < N; i++)
                    if (isNeighbor(i))
                        s.sendMessage(i, "invite", level + 1);
            }
            s.nextPulse();
        }
    }
    public void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("invite")) {
            if (parent == -1) {
                parent = src;
                level = m.getMessageInt();
                Util.println(myId + " is at level " + level);
                for (int i = 0; i < N; i++)
                    if (isNeighbor(i) && (i != src))
                        s.sendMessage(i, "invite", level + 1);
            }
        }
    }
}
