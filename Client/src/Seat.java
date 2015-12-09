import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Seat extends UnicastRemoteObject implements I_Seat {

    private I_Fork leftFork;
    private I_Fork rightFork;
    final int id;

    private final int maxTriesForRightFork = 10;

    @Override
    public ReentrantLock getLock() throws RemoteException {
        return lock;
    }

    @Override
    public int getId() throws RemoteException {
        return id;
    }

    private final ReentrantLock lock = new ReentrantLock();
    boolean freeForSeatChoice = true;

    public Seat(int id, Fork leftFork, Fork rightFork) throws RemoteException {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.id = id;
    }

    @Override
    public boolean takeRightFork() throws InterruptedException, RemoteException {
        int tmpTries = 0;
        while (tmpTries < maxTriesForRightFork) {
            if (rightFork.take()) return true;
//            try {
            Thread.sleep(new Random().nextInt(5));
            tmpTries++;
//            } catch (InterruptedException e) {
//                return;
//            }
        }
        return false;
    }

    public boolean takeLeftFork() throws InterruptedException, RemoteException {
        return leftFork.take();
    }

    public void dropRight() throws RemoteException {
        rightFork.drop();
    }

    public void dropLeft() throws RemoteException {
        leftFork.drop();
    }

    public I_Fork getRightFork() throws RemoteException {
        return rightFork;
    }

    public void rebindRightFork(I_Fork f) throws RemoteException {
        // first we need to ensure that the seat will not be taken by a new philosopher
        freeForSeatChoice = false;

        // wait if some philosophers are waiting to eat on that seat
        while (lock.getQueueLength() > 0 || lock.isLocked()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // nothing happens in here
            }
        }
        rightFork = f;

        // Allow seat to be taken by philo
        freeForSeatChoice = true;
    }


    @Override
    public String toString() {
        return "Seat#" + id + " ".concat(leftFork.toString().concat(rightFork.toString()));
    }

    public void standUp() throws RemoteException {
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
        }
    }
}
