import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_Table extends Remote {

    boolean isAllowedToEat(Philosopher p) throws RemoteException;

    void addSeats(int nSeatCount) throws RemoteException;


    I_Fork getFirstFork() throws RemoteException;

    int getQueueLength() throws RemoteException;

    void removeSeats(int nSeatCount) throws RemoteException;


    Seat takeSeat(boolean tableMasterIsAsking) throws InterruptedException, RemoteException;

    String getName() throws RemoteException;

    int getMaxMealsEaten()throws RemoteException;

    void connectWithOtherTable(I_Table otherTable)  throws RemoteException;
}