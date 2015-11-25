import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_Fork extends Remote {

    boolean take() throws InterruptedException, RemoteException;

    void drop() throws RemoteException;
}
