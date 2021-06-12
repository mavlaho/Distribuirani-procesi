import java.util.TreeSet;
import java.util.Iterator;
import java.util.HashMap;

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
     // lastLayer je skup za vanjski layer trenutnog klastera,
    // newLayer je skup za sve dostupne susjede,
    // tj. potencijalan novi sloj.
    TreeSet<Integer> newLayer = new TreeSet<Integer>();
    TreeSet<Integer> lastLayer = new TreeSet<Integer>();
    // S kojim klasterima ovaj klaster ima preferirane bridove.
    TreeSet<Integer> clustPrefEdge = new TreeSet<Integer>();
    // Za koliko preferiranih edgeva se čeka odgovor.
    int numWaitingPrefEdge = 0;
    // Pomočna mapa za stvaranje preferiranih edgeva.
    HashMap<Integer,Integer> clustNodeMap = new HashMap<Integer, Integer>();

    // k je parametar za veličine klastera.
    public SpanForest(Linker initComm, int kPar) {
        super(initComm);
        k = kPar;

        // Radi lakše implementacije, fiksiram proces 0 kao prvog vođu klastera.
        if (myId == 0) createTree();

        // Ostali procesi čekaju da budu pozvani u klaster.
    }

    public synchronized void waitForDone() {
	    while (!done) myWait();
    }

    // Čeka dok se ne dobi odgovor od svih vrhova iz zadnjeg layera.
    public synchronized void waitForAvailable() {
        while (numWaiting > 0) myWait();
    }

    // Stvara novo stablo/klaster, s čvorom koji je pozvao ovu metodu
    // kao korijenom.
    public synchronized void createTree() {
        parent = myId;
        clusterLeader = myId;
        newLayer.add(myId);
        expandCluster();
    }

    private void expandCluster() {
        if (newLayer.size() > (k-1) * clusterSize) {
            // Prva "runda".
            if (clusterSize == 0) {
                ++clusterSize;
                lastLayer.add(myId);
                newLayer.clear();
                available(myId);
                notifyAll();
            } else { // Iduće "runde".
                numWaiting = lastLayer.size();
                Iterator<Integer> t = lastLayer.iterator();
                while (t.hasNext()) {
                    Integer node = (Integer) t.next();
                    // Ova poruka znači da se daje dopuštenje da invitaju susjede u klaster.
                    if (node != myId) sendMsg(node.intValue(), "startInviting");
                    else { // Vrh ne može sam sebi poslati poruku.
                        numWaiting = 0;
                        startInviting(myId);
                    }
                }
                notifyAll();
            }
        } else {
            // Sada idem svoriti preferirane vrhove.
            numWaiting = lastLayer.size();
            Iterator<Integer> t = lastLayer.iterator();
            while (t.hasNext()) {
                Integer node = (Integer) t.next();
                // Ova poruka znači da se traži od vrha da traži potencijalne preferirane vrhove.
                if (node != myId) sendMsg(node.intValue(), "startPrefEdgeCreation");
                else { // Vrh ne može sam sebi poslati poruku.
                    numWaiting = 0;
                    startPrefEdgeCreation(myId);
                }
            }
        }
    }

    private void expandCluster2() {
        lastLayer.clear();
        lastLayer.addAll(newLayer);
        clusterSize += lastLayer.size();

        // Konstruiram novi newLayer.
        newLayer.clear();
        numWaiting = lastLayer.size();
        var t = lastLayer.iterator();
        while (t.hasNext()) {
            Integer node = (Integer) t.next();
            // Ova poruka znači da se pošalju svi susjedi koji mogu postati
            // potencijalni članovi klastera.
            sendMsg(node.intValue(), "available");
        }
        notifyAll();
    }

    private void available(int src) {
        for (int i = 0; i < N; i++) 
            if ((i != myId) && (i != src) && isNeighbor(i)) {
                ++numWaiting;
                // Upit pripada li ovaj node nekom klasteru.
                sendMsg(i, "isInCluster");
            }
        
        // Ako nikome nije bila poslana poruka.
        if (numWaiting == 0) {
            if (myId != clusterLeader) {
                // Ova poruka klaster leaderu označava da je primio sve susjede od
                // ovog vrha koji ne pripadaju ni jednom klasteru.
                sendMsg(clusterLeader, "allAvailableSent");
                notify();
            } else {
                // Dobiveni su svi odgovori, nastavlja se klaster.
                expandCluster();
            }
        } else notifyAll();
    }

    private void createNextCluster() {
        // Prosljeđuje poruku idućem vrhu, tj. javlja svim
        // vrhovima da je stablo gotovo ako nema idućeg čvora.
        // Ovo znači da je ovo zadnji vrh, tj. sada su svi vrhovi u klasterima.
        if (myId + 1 == N) {
            // Javlja svim ostalim vrhovima da je konstrukcija šume gotova.
            for (int i = 0; i < myId; ++i) {
                sendMsg(i, "spanForestDone");
                done = true;
                notifyAll();
            }
        } else {
            sendMsg(myId+1, "createCluster");
            notifyAll();
        }
    }

    void startInviting(int src) {
        for (int i = 0; i < N; i++) 
            if ((i != myId) && (i != src) && isNeighbor(i)) {
                ++numWaiting;
                // Upit za ući u klaster (kao dijete ovog čvora).
                sendMsg(i, "invite", clusterLeader);
            }

        if (numWaiting == 0) {
            if (myId != clusterLeader) sendMsg(clusterLeader, "allInvitesSent");
            else expandCluster2();
        } else notifyAll();
    }

    void startPrefEdgeCreation(int src) {
        for (int i = 0; i < N; i++) 
            if ((i != myId) && (i != src) && isNeighbor(i)) {
                ++numWaiting;
                // Upit za ući u klaster (kao dijete ovog čvora).
                sendMsg(i, "invitePrefEdge", clusterLeader);
            }

        if (numWaiting == 0) {
            if (myId != clusterLeader) sendMsg(clusterLeader, "allInvitesSentPrefEdge");
            else createNextCluster();
        } else notifyAll();
    }

    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("available")) {
            available(src);
        } else if (tag.equals("isInCluster")) {
            sendMsg(src, "isInClusterResponse",  clusterLeader);
        } else if (tag.equals("isInClusterResponse")) {
            // Ovo u poruci bi trebao biti id čvora koji je vođa klastera.
            if (m.getMessageInt() == -1) {
                // Ova poruka označava da je src potencijalni novi član klastera.
                if (myId != clusterLeader) sendMsg(clusterLeader, "potentialMember", src);
                else newLayer.add(src);
            }

            // Ako su stigli svi odgovori.
            if (--numWaiting == 0) {
                if (myId != clusterLeader) {
                    // Ova poruka klaster leaderu označava da je primio sve susjede od
                    // ovog vrha koji ne pripadaju ni jednom klasteru.
                    sendMsg(clusterLeader, "allAvailableSent");
                } else {
                    // Dobiveni su svi odgovori, nastavlja se klaster.
                    expandCluster();
                }
            }
            
        } else if (tag.equals("potentialMember")) {
            newLayer.add(m.getMessageInt());
        } else if (tag.equals("allAvailableSent")) {
            if (--numWaiting == 0) {
                // Dobiveni su svi odgovori, nastavlja se klaster.
                expandCluster();
            }
        } else if (tag.equals("startInviting")) {
            startInviting(src);    
        } else if (tag.equals("invite")) {
            if (parent == -1) {
                parent = src;
                clusterLeader = m.getMessageInt();
                sendMsg(src, "accept");
            } else
                sendMsg(src, "reject", clusterLeader);
        } else if ((tag.equals("accept")) || (tag.equals("reject"))) {
            if (tag.equals("accept")) {
                children.add(src);
            }

            if (--numWaiting == 0) {
                // Javlja klaster leaderu da je gotov.
                if (myId != clusterLeader) sendMsg(clusterLeader, "allInvitesSent");
                else expandCluster2();
                notifyAll();
            }
        } else if (tag.equals("spanForestDone")) {
            done = true;
            notifyAll();
        } else if (tag.equals("createCluster")) {
            // Ako nije član klastera.
            if (clusterLeader == -1) {
                createTree();
            } else {
                createNextCluster();
            }
        } else if (tag.equals("allInvitesSent")) {
            expandCluster2();
        } else if (tag.equals("startPrefEdgeCreation")) {
            startPrefEdgeCreation(src);
        } else if (tag.equals("invitePrefEdge")) {
            sendMsg(src, "invitePrefEdgeResponse", clusterLeader);
        } else if (tag.equals("invitePrefEdgeResponse")) {
            if (m.getMessageInt() != clusterLeader && m.getMessageInt() != -1 &&
                        !clustNodeMap.containsKey(m.getMessageInt())) {
                // Spremi src za kasnije, ako vođa klastera dozvoli
                // da se preferirani brid napravi.
                clustNodeMap.put(m.getMessageInt(), src);
                // Javlja vođi klastera da je pronađen potencijalni preferirani brid.
                if (myId != clusterLeader) sendMsg(clusterLeader, "potentialPreferedEdge", m.getMessageInt());
                else {
                    if (!clustPrefEdge.contains(m.getMessageInt())) {
                        clustPrefEdge.add(m.getMessageInt());
                        prefEdge.add(src);
                        sendMsg(src, "prefEdgeNotice");
                    }
                }
            }

            if (--numWaiting == 0) {
                if (myId != clusterLeader) sendMsg(clusterLeader, "allInvitesSentPrefEdge");
                else createNextCluster();
            }
        } else if (tag.equals("potentialPreferedEdge")) {
            if (!clustPrefEdge.contains(m.getMessageInt())) {
                clustPrefEdge.add(m.getMessageInt());
                // Javlja vrhu da doda brid kao preferirani brid.
                sendMsg(src, "prefEdgeAccept", m.getMessageInt());
            }
        } else if (tag.equals("prefEdgeAccept")) {
            // v je susjedni vrh iz klastera m.getMessageInt().
            Integer v = clustNodeMap.get(m.getMessageInt());
            prefEdge.add(v.intValue());
            // Javlja susjedu da uspostavi prefEdge sa svoje strane.
            sendMsg(v.intValue(), "prefEdgeNotice");
        } else if (tag.equals("prefEdgeNotice")) {
            prefEdge.add(src);
        } else if (tag.equals("allInvitesSentPrefEdge")) {
            if (--numWaiting == 0) createNextCluster();
        }
    }
}
