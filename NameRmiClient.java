import java.rmi.*;
public class NameRmiClient {
    public static void main(String args[]) {
        try {
            NameService r = (NameService) 
                           Naming.lookup("rmi://linux02/MyNameServer");
            int i = r.insert("p1", "tick.ece", 2058);
            int j = r.search("p1");
            if (j != -1)
               System.out.println(r.getHostName(j) +":" + r.getPort(j));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
