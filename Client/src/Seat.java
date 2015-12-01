import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Seat {

    private Fork leftFork;
    private Fork rightFork;
    final int id;
    final ReentrantLock lock = new ReentrantLock();
    boolean freeForSeatChoice = true;

    public Seat(int id, Fork leftFork, Fork rightFork) {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.id = id;
    }

    public void takeRightFork() throws InterruptedException, RemoteException {
        while(!rightFork.take()){
//            try {
                Thread.sleep(new Random().nextInt(5));
//            } catch (InterruptedException e) {
//                return;
//            }
        }
    }
    public boolean takeLeftFork() throws InterruptedException, RemoteException{
        return leftFork.take();
    }
    public void dropRight() throws RemoteException {
        rightFork.drop();
    }

    public void dropLeft() throws RemoteException {
        leftFork.drop();
    }

    public Fork getRightFork() { return rightFork; }

    public void rebindRightFork(Fork f) {
        this.rightFork = f;
    }


    @Override
    public String toString() {
        return "Seat#" + id + " ".concat(leftFork.toString().concat(rightFork.toString()));
    }

    public void standUp() {
        lock.unlock();
    }
}
