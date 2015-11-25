import java.util.concurrent.TimeUnit;

public class Fork extends Lockable {

    public boolean take() throws InterruptedException{
        return lock.tryLock(1, TimeUnit.MILLISECONDS);
    }

    public void drop() {
        try {
            lock.unlock();
        } catch (Exception e) {
        }
    }
}
