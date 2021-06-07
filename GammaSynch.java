import java.util.LinkedList;

public class GammaSynch extends Process implements Synchronizer {
    // Kopirano iz BetaSynch. Postepeno treba prilagođavati
    // varijabe, tj. preimenovati.
    int acksNeeded;
    IntLinkedList unsafeKids = new IntLinkedList();
    MsgHandler prog;
    private boolean pulseMsg;

    // Ove su prilagođene za gamma sync.
    int pulse;
    SpanForest forest;

    // k je argument za klasteriranje.
    public GammaSynch(Linker initComm, int k) {
        super(initComm);
        forest = new SpanForest(comm, k);
        pulse = 0;
    }
}