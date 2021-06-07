import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
public class NameServiceImpl extends UnicastRemoteObject
        implements NameService {
    final int maxSize = 100;
    private String[] names = new String[maxSize];
    private String[] hosts = new String[maxSize];
    private int[] ports = new int[maxSize];
    private int dirsize = 0;
    public NameServiceImpl() throws RemoteException {
    }
    public int search(String s) throws RemoteException {
        for (int i = 0; i < dirsize; i++)
            if (names[i].equals(s)) return i;
        return -1;
    }
    public int insert(String s, String hostName, int portNumber)
            throws RemoteException {
        int oldIndex = search(s); // is it already there
        if ((oldIndex == -1) && (dirsize < maxSize)) {
            names[dirsize] = s;
            hosts[dirsize] = hostName;
            ports[dirsize] = portNumber;
            dirsize++;
            return 1;
        } else
            return 0;
    }
    public int getPort(int index) throws RemoteException {
        return ports[index];
    }
    public String getHostName(int index) throws RemoteException {
        return hosts[index];
    }
    public static void main(String args[]) {
        // create security manager
        System.setSecurityManager(new RMISecurityManager());
        try {
            NameServiceImpl obj = new NameServiceImpl();
            Naming.rebind("MyNameServer", obj);
            System.out.println("MyNameServer bound in registry");
        } catch (Exception e) {
            System.out.println("NameServiceImpl err: " + e.getMessage());
        }
    }
}

