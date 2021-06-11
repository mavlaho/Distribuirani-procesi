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
    public void createTree() {
        parent = myId;
        clusterLeader = myId;
        
        newLayer.add(myId);
        while (newLayer.size() >= (k-1) * clusterSize) {
            // Prva "runda" petlje.
            if (clusterSize == 0) {
                ++clusterSize;
                lastLayer.add(myId);
                newLayer.clear();
                available(myId);
                // Čeka numWaiting poruka (svaka od tih poruka poziva notify).
                // TODO: implementirati LOCK interface.
                while (numWaiting-- > 0) {
                    Util.println(((Integer)numWaiting).toString());
                    myWait();
                }
                Util.println("OUT");
                numWaiting = 0;
            } else { // Iduće "runde" petlje.
                numWaiting = lastLayer.size();
                Iterator<Integer> t = lastLayer.iterator();
                while (t.hasNext()) {
                    Integer node = (Integer) t.next();
                    // Ova poruka znači da se daje dopuštenje da invitaju susjede u klaster.
                    sendMsg(node.intValue(), "startInviting");
                }

                lastLayer.clear();
                lastLayer.addAll(newLayer);
                clusterSize += lastLayer.size();
                waitForAvailable();

                // Konstruiram novi newLayer.
                newLayer.clear();
                numWaiting = lastLayer.size();
                t = lastLayer.iterator();
                while (t.hasNext()) {
                    Integer node = (Integer) t.next();
                    // Ova poruka znači da se pošalju svi susjedi koji mogu postati
                    // potencijalni članovi klastera.
                    sendMsg(node.intValue(), "available");
                }
                waitForAvailable();
            }
        }


        // Sada je klaster dovršen i krećem s kreiranjem novog klastera.

        // Ovo znači da je ovo zadnji vrh, tj. sada su svi vrhovi u klasterima.
        if (myId + 1 == N) {
            // Javlja svim ostalim vrhovima da je konstrukcija šume gotova.
            for (int i = 0; i < myId; ++i) {
                sendMsg(i, "spanForestDone");
            }
            done = true;
            notify();
        } else {
            // Ovaj klaster je gotov, sada se idućem vrhu šalje poruka da napravi
            // novi klaster (ako je on već dio klastera, šalje poruku idućem vrhu).
            sendMsg(myId+1, "createCluster");
        }
    }

    private void available(int src) {
        for (int i = 0; i < N; i++) 
                if ((i != myId) && (i != src) && isNeighbor(i)) {
                    ++numWaiting;
                    // Upit pripada li ovaj node nekom klasteru.
                    sendMsg(i, "isInCluster");
                }
    }

    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("available")) {
            available(src);
        } else if (tag.equals("isInCluster")) {
            sendMsg(src, "isInClusterResponse",  clusterLeader);
            notifyAll();
        } else if (tag.equals("isInClusterResponse")) {
            // TODO: provjeriti je li se poruka dobro šalje.
            // Ovo u poruci bi trebao biti id čvora koji je vođa klastera.
            if (m.getMessageInt() == -1) {
                // Ova poruka označava da je src potencijalni novi član klastera.
                if (myId != clusterLeader) sendMsg(clusterLeader, "potentialMember", src);
                else newLayer.add(m.getMessageInt());
            }

            // Ako su stigli svi odgovori.
            if (--numWaiting == 0) {
                // Ova poruka klaster leaderu označava da je primio sve susjede od
                // ovog vrha koji ne pripadaju ni jednom klasteru.
                if (myId != clusterLeader) sendMsg(clusterLeader, "allAvailableSent");
            }
        } else if (tag.equals("potentialMember")) {
            // TODO: provjeriti je li se poruka dobro šalje.
            newLayer.add(m.getMessageInt());
        } else if (tag.equals("allAvailableSent")) {
            if (--numWaiting == 0) {
                notify();
            }
        } else if (tag.equals("startInviting")) {
            ++numReports;
            for (int i = 0; i < N; i++) 
                if ((i != myId) && (i != src) && isNeighbor(i)) {
                    ++numWaiting;
                    // Upit za ući u klaster (kao dijete ovog čvora).
                    sendMsg(i, "invite");
                }     
        } else if (tag.equals("invite")) {
            if (parent == -1) {
                parent = src;
                sendMsg(src, "accept");
            } else
                sendMsg(src, "reject", clusterLeader);
        } else if ((tag.equals("accept")) || (tag.equals("reject"))) {
            if (tag.equals("accept")) {
                children.add(src);
            } else if (m.getMessageInt() != clusterLeader && 
                        !clustNodeMap.containsKey(m.getMessageInt())) {
                // Spremi src za kasnije, ako vođa klastera dozvoli
                // da se preferirani brid napravi.
                clustNodeMap.put(m.getMessageInt(), src);
                // Javlja vođi klastera da je pronađen potencijalni preferirani brid.
                sendMsg(clusterLeader, "potentialPreferedEdge", m.getMessageInt());
            }
            if (--numWaiting == 0) {
                // Javlja klaster leaderu da je gotov.
                sendMsg(clusterLeader, "allAvailableSent");
                notify();
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
        } else if (tag.equals("spanForestDone")) {
            done = true;
            notify();
        } else if (tag.equals("createCluster")) {
            // Ako nije član klastera.
            if (clusterLeader == -1) {
                createTree();
            } else {
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
                }
            }
        }
    }
}
