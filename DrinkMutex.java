public class DrinkMutex extends Process implements Lock {
    private static final int tranquil = 0, thirsty = 1, drinking = 2;
    boolean bottle[] = null,  requestBottle[] = null, needBottle[] = null; 
    DinMutex din;
    int myState = tranquil;
    boolean eating = false;
    public DrinkMutex(Linker initComm) {
        super(initComm);
        din = new DinMutex(initComm); /* create diner instance for each drinker */
        bottle = new boolean[N];
        requestBottle = new boolean[N];
        needBottle = new boolean[N];
        for (int i = 0; i < N; i++) {
            if ((myId > i) && (isNeighbor(i))) {
                bottle[i] = false; requestBottle[i] = true;
            } else {
                bottle[i] = true; requestBottle[i] = false;
            }
        }
    }
    public synchronized void requestCS() {
        myState = thirsty;
        /* following for testing only - pass in required resources array instead */
        needBottle[myId] = true;
        if (haveBottles()) myState = drinking;
        else {
            din.requestCS(); 		/* force diner to hungry state */
            eating = true;
            for (int i = 0; i < N; i++)
                if (needBottle[i] && requestBottle[i] && !bottle[i]) {
                    sendMsg(i, "Request");
                    requestBottle[i] = false;
                }
		}
        while (myState != drinking) myWait();
    }
    public synchronized void releaseCS() {
        myState = tranquil;
        if (eating) {
		eating = false;
		din.releaseCS();  /* force diner to thinking state */
         }
		     
        for (int i = 0; i < N; i++) {
            needBottle[i] = false; 	/* clear required resources array */
            if (requestBottle[i]) {
				sendMsg(i, "Bottle");
				bottle[i] = false;
			}
        }
    }
    boolean haveBottles() {
        for (int i = 0; i < N; i++)
            if (needBottle[i] && !bottle[i]) return false;
        return true;
    }
    public synchronized void handleMsg(Msg m, int source, String tag) {
        if (tag.equals("Request")) {
            requestBottle[source] = true;
            if (!needBottle[source]) {
				sendMsg(source, "Bottle");
				bottle[source] = false;
			}
            else if ((myState != drinking) && !din.fork[source]) {
				sendMsg(source, "Bottle");
				bottle[source] = false;
				if (needBottle[source]){
                    sendMsg(source, "Request");
                    requestBottle[source] = false;
                }
            }
        }
        else if (tag.equals("Bottle")) {
            bottle[source] = true;
            if (haveBottles()) {
                myState = drinking;
                notify();
            }
        }
    }
}
