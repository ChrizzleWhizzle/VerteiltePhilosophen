import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Chris on 02.12.2015.
 */
public interface I_Master extends Remote {
    void connectToServermasterAndAddOwnTable(String serverMasterIP) throws RemoteException;

    void addPhilosophers(int nNormalPhils, int nHungryPhils) throws RemoteException;

    void addSeats(int nSeatsToBeAdded) throws RemoteException;

    Table getTable() throws RemoteException;

    boolean isAllowedToEat(Philosopher phil) throws RemoteException;

    boolean removeSeats(int nSeatsToBeDeleted) throws RemoteException;

    void startTheFeeding() throws RemoteException;

    void stopTheFeeding() throws RemoteException;

    I_Seat takeSeat(I_Seat compareToThisSeat) throws InterruptedException, RemoteException;

    List<Philosopher> getPhilosophers() throws RemoteException;
}
