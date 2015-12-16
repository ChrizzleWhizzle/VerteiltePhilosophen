import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Seat extends UnicastRemoteObject implements I_Seat {

    private I_Fork leftFork;
    private I_Fork rightFork;
    final int id;

    private final ReentrantLock lock = new ReentrantLock();
    private boolean freeForSeatChoice = true;

    private final int maxTries = 10;



    public Seat(int id, Fork leftFork, Fork rightFork) throws RemoteException {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.id = id;
    }

    @Override
    public int getId() throws RemoteException {
        return id;
    }

    @Override
    public ReentrantLock getLock() throws RemoteException {
        return lock;
    }

    @Override
    public boolean isFreeForSeatChoice() throws RemoteException  {
        return freeForSeatChoice;
    }

    @Override
    public synchronized void setFreeForSeatChoice(boolean freeForSeatChoice) throws RemoteException {
        this.freeForSeatChoice = freeForSeatChoice;
    }

    @Override
    //try to take right fork for maxTries times
    public boolean takeRightFork() throws InterruptedException, RemoteException {
        int tmpTries = 0;
        while (tmpTries < maxTries) {
            if (rightFork.take()){
                return true;
            }
            Thread.sleep(new Random().nextInt(5));
            tmpTries++;

        }
        return false;
    }

    @Override
    public boolean takeLeftFork() throws InterruptedException, RemoteException {
        return leftFork.take();
    }

    @Override
    public void dropRight() throws RemoteException {
        rightFork.drop();
    }

    @Override
    public void dropLeft() throws RemoteException {
        leftFork.drop();
    }

    @Override
    public synchronized I_Fork getRightFork() throws RemoteException {
        return rightFork;
    }

    @Override
    public synchronized void rebindRightFork(I_Fork f) throws RemoteException {
        // first we need to ensure that the seat will not be taken by a new philosopher
        freeForSeatChoice = false;

        // wait if some philosophers are waiting to eat on that seat
        while (lock.getQueueLength() > 0 || lock.isLocked()) {
            try {
                Thread.sleep(new Random().nextInt(10));
            } catch (InterruptedException e) {
                // nothing happens in here
            }
        }
        rightFork = f;

        // Allow seat to be taken by philo
        freeForSeatChoice = true;
    }

    @Override
    public synchronized boolean sitDown() throws InterruptedException, RemoteException {
        int tmpTries = 0;
        while (tmpTries < maxTries) {
            if (lock.tryLock(new Random().nextInt(5), TimeUnit.MILLISECONDS)){
                return true;
            }
            tmpTries++;

        }
        return false;
    }

    @Override
    public String toString() {
        return "Seat#" + id + " ".concat(leftFork.toString().concat(rightFork.toString()));
    }

    @Override
    public void standUp() throws RemoteException {
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
        }
    }
}
