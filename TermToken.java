import java.util.*;
public class TermToken extends Process implements TermDetector {
    final static int passive = 0, active = 1, white = 0, black = 1;
    int state = passive, color = white;
    int c = 0;  
    int next;
    boolean haveToken = false;
    int tokenCount = 0, tokenColor = white;  
    public TermToken(Linker initComm) {
        super(initComm);
        next = (myId + 1) % N;      
    }
    public synchronized void initiate() {
        if (myId == Symbols.coordinator) {
            if (state == passive) sendToken();
            else haveToken = true;
        }
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("termToken")) {
            haveToken = true;
	    StringTokenizer st = new StringTokenizer(m.getMessage());
 	    tokenColor = Integer.parseInt(st.nextToken());
 	    tokenCount = Integer.parseInt(st.nextToken());
            if (myId == Symbols.coordinator) {
                if ((c + tokenCount == 0) && (color == white) &&
                        (state == passive) && (tokenColor == white)) {
                    System.out.println("Termination Detected");
                    haveToken = false;
                }
            }
            if ((state == passive) && haveToken) sendToken();
        } else { // application message
            state = active;
            color = black;
            c = c - 1;
        }
    }
    public synchronized void sendAction() {
        c = c + 1;
    }
    public synchronized void turnPassive() {
        state = passive;
        if (haveToken) sendToken();
    }
    void sendToken() {
        if (myId == Symbols.coordinator)
            sendMsg(next, "termToken", white, 0);
        else if ((color == black) || (tokenColor == black))
            sendMsg(next, "termToken", black, c + tokenCount);
        else
            sendMsg(next, "termToken", white, c + tokenCount);
        haveToken = false;
        color = white;
    }
}
