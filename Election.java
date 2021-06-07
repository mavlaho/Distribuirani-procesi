public interface Election extends MsgHandler {
    void startElection();
    int getLeader();//blocks till the leader is known
}
