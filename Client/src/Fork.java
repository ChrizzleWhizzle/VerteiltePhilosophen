import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Fork extends UnicastRemoteObject
        implements I_Fork {

    final ReentrantLock lock = new ReentrantLock();

    public Fork()
            throws RemoteException {
    }

    @Override
    public synchronized boolean take() throws InterruptedException, RemoteException {
        return lock.tryLock(1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void drop() throws RemoteException {
        try {
            lock.unlock();
        } catch (Exception e) {
        }
    }
}
