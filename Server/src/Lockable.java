import java.util.concurrent.locks.ReentrantLock;

public abstract class Lockable {

    final ReentrantLock lock = new ReentrantLock();

}
