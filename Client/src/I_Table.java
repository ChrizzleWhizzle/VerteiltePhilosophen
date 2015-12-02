import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Chris on 02.12.2015.
 */
public interface I_Table extends Remote {

    boolean isAllowedToEat(Philosopher p) throws RemoteException;

    void addSeats(int nSeatCount) throws RemoteException;


    Fork getFirstFork() throws RemoteException;

    int getQueueLength() throws RemoteException;

    void removeSeats(int nSeatCount) throws RemoteException;


    Seat takeSeat(boolean tableMasterIsAsking) throws InterruptedException, RemoteException;

    String getName() throws RemoteException;
}