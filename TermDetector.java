public interface TermDetector {
    public void initiate();
    public void sendAction();
    public void turnPassive();
    public void handleMsg(Msg m, int srcsId, String tag);
}
