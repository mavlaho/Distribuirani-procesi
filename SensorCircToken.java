public class SensorCircToken extends CircToken
implements MsgHandler, SensorUser {
    VCLinker comm;
    Sensor checker;
    int coordinator;
    int algoCode;
    public SensorCircToken(VCLinker comm, int coordinator, int algoCode){
        super(comm, coordinator);
        this.comm = comm;
        this.coordinator = coordinator;
        this.algoCode = algoCode;
    }
    public void initiate() {
        if (algoCode == 0)
            checker = new CentSensor(comm, this);
        else
            checker = new DistSensor(comm, this);
        if (!haveToken) checker.localPredicateTrue(comm.vc);
        super.initiate();
    }
    public synchronized void sendToken() {
        super.sendToken();
        if (!haveToken) checker.localPredicateTrue(comm.vc);
    }
    public synchronized void handleMsg(Msg m, int src, String tag){
        checker.handleMsg(m, src, tag);
        super.handleMsg(m, src, tag);
    }
    public void globalPredicateTrue(int v[]){
        System.out.println("**************************************");
        System.out.println("Predicate true at:" + Util.writeArray(v));
    }
    public void globalPredicateFalse(int pid){
        System.out.println("**************************************");
        System.out.println("Predicate false. Proc " + pid + " finished");
    }
}
