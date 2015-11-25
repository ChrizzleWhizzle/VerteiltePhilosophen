import java.util.Random;

public class Seat extends Lockable {

    private Fork leftFork;
    private Fork rightFork;
    final int id;

    public Seat(int id, Fork leftFork, Fork rightFork) {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.id = id;
    }

    public void takeRightFork() throws InterruptedException{
        while(!rightFork.take()){
//            try {
                Thread.sleep(new Random().nextInt(5));
//            } catch (InterruptedException e) {
//                return;
//            }
        }
    }
    public boolean takeLeftFork() throws InterruptedException{
        return leftFork.take();
    }
    public void dropRight(){
        rightFork.drop();
    }

    public void dropLeft(){
        leftFork.drop();
    }


    @Override
    public String toString() {
        return super.toString().concat(leftFork.toString().concat(rightFork.toString()));
    }

    public void standUp() {
        lock.unlock();
    }
}
