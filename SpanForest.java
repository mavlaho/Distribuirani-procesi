import java.util.TreeSet;
import java.util.Iterator;

public class SpanForest extends Process{
    public int parent = -1;
    public int clusterLeader = -1;
    public IntLinkedList children = new IntLinkedList();
    // Lista čvorova s kojima ovaj čvor ima preferirani brid.
    public IntLinkedList prefEdge = new IntLinkedList();
    int numReports = 0;
    boolean done = false;
    int k;
    // Veličina trenutnog klastera.
    int clusterSize = 0;
    // Broj procesa čiji se odgovor čeka.
    int numWaiting = 0;

    // k je parametar za veličine klastera.
    public SpanForest(Linker initComm, int kPar) {
        super(initComm);
        k = kPar;

        // Radi lakše implementacije, fiksiram proces 0 kao prvog vođu klastera.
        if (myId == 0) createTree();

        // Ostali procesi čekaju da budu pozvani u klaster.
    }

    // Bloka sve dok se ne nađe pripadni klaster,
    // tj. završi kreiranje klastera, ako se radi o vođi klastera.
    public synchronized void waitForDone() {
	    while (!done) myWait();
    }

    // Čeka dok se ne dobi odgovor od svih vrhova iz zadnjeg layera.
    public synchronized void waitForAvailabe() {
        while (numWaiting > 0) myWait();
    }

    // Stvara novo stablo/klaster, s čvorom koji je pozvao ovu metodu
    // kao korijenom.
    public synchronized void createTree() {
        parent = myId;
        clusterLeader = myId;
        
        // lastLayer je skup za vanjski layer trenutnog klastera,
        // newLayer je skup za sve dostupne susjede,
        // tj. potencijalan novi sloj.
        TreeSet<Integer> newLayer = new TreeSet<Integer>();
        TreeSet<Integer> lastLayer = new TreeSet<Integer>();
        newLayer.add(myId);
        while (newLayer.size() > (k-1) * clusterSize) {
            // Prva "runda" petlje.
            if (clusterSize == 0) {
                lastLayer.add(myId);
                newLayer.clear();
            } else { // Iduće "runde" petlje.
                // TODO: Dodaj sve iz newLayer-a u klaster.
            }

            // Konstruiram novi newLayer.
            Iterator<Integer> t = lastLayer.iterator();
            while (t.hasNext()) {
                Integer node = (Integer) t.next();
                // Ova poruka znači da se pošalju svi susjedi koji mogu postati
                // potencijalni članovi klastera.
                ++numWaiting;
                sendMsg(node.intValue(), "available");
            }
            waitForAvailabe();
        }
    }

    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("available")) {
            // Dvije petlje jer prvo brojim koliko poruka šaljem,
            // pa ih tek nakon toga šaljem.
            for (int i = 0; i < N; i++) 
                if ((i != myId) && (i != src) && isNeighbor(i)) ++numWaiting;
            for (int i = 0; i < N; i++) 
                if ((i != myId) && (i != src) && isNeighbor(i)) 
                    // Upit pripada li ovaj node nekom klasteru.
                    sendMsg(i, "isInCluster");
        } else if (tag.equals("isInCluster")) {
            sendMsg(src, "isInClusterResponse",  clusterLeader);
        } else if (tag.equals("isInClusterResponse")) {
            // TODO: obraditi slučaj kada je u klasteru 
            //       (poslati tu informaciju klaster leaderu)

            // Ako su stigli svi odgovori.
            if (--numWaiting == 0) {
                // Ova poruka klaster leaderu označava da je primio sve susjede od
                // ovog vrha koji ne pripadaju ni jednom klasteru.
                sendMsg(clusterLeader, "allAvailableSent");
            }
        }
    }
}
