public class CamCircToken extends CircToken implements CamUser {
    public CamCircToken(Linker initComm, int coordinator) {
        super(initComm, coordinator);
    }
    public synchronized void localState() {
        Util.println("local state: haveToken=" + haveToken);
    }
}
