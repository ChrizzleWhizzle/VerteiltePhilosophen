import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;

public interface I_Seat extends Remote {


    ReentrantLock lock = new ReentrantLock();
    boolean freeForSeatChoice = true;

    int getId() throws RemoteException;

    boolean takeRightFork() throws InterruptedException, RemoteException;

    boolean takeLeftFork() throws InterruptedException, RemoteException;

    void dropRight() throws RemoteException;

    void dropLeft() throws RemoteException;

    I_Fork getRightFork() throws RemoteException;

    void rebindRightFork(I_Fork f) throws RemoteException;


    void standUp() throws RemoteException;

    ReentrantLock getLock() throws RemoteException;
    }
