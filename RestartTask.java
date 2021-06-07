import java.util.TimerTask;
public class RestartTask extends TimerTask {
    MsgHandler app;
    public RestartTask(MsgHandler app) {
        this.app = app;
    }
    public void run() {
        app.handleMsg(null, 0, "restart");
    }
}
