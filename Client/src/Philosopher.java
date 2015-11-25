import java.util.concurrent.locks.ReentrantLock;

public class Philosopher extends Thread {
    // Process:
    // start with meditate
    // if hungry go to dining room
    // take a free seat
    // take right fork (only one Philosopher can take the fork at time)
    // take left fork (only one Philosopher can take the fork at time)
    // eat
    // go back to meditate
    // after 3 meals go to sleep
    // (rave)
    // repeat
    // horst

    public PhilosopherState state;
    private static int event=0;

    private int id;
    private Seat seat;
    private Table table;
    private int mealsEaten;
    public int totalMealsEaten;
    private boolean hasBothForks = false;

    public Philosopher(int id, Table table, PhilosopherState state) {
        this.id = id;
        this.table = table;
        this.state = state;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (table.master.isAllowedToEat(this)) {
                    takeSeat();
                    // take forks
                    while (!hasBothForks) {
                        seat.takeRightFork();
                        if (!seat.takeLeftFork()) {
                            seat.dropRight();
                        } else {
                            hasBothForks = true;
                        }
                    }
                    ; //boolean hasbothforks
                    eat();
                    seat.dropLeft();
                    seat.dropRight();
                    seat.standUp();
                    meditate();
                    if (mealsEaten == state.getMaxMealsEaten()) goToSleep();
                } else {
                    sleepBan();
                }
            }
        } catch (Exception e) {
            return;
        }
    }

    private void eat() throws InterruptedException {
            postMsg("eating for " + state.getEatTime() + "on Seat: " + seat.id);
            mealsEaten++;
            totalMealsEaten++;
            Thread.sleep(state.getEatTime());
    }

    private void goToSleep() throws InterruptedException {
            postMsg("sleeping for " + state.getSleepTime());
            Thread.sleep(state.getSleepTime());
            mealsEaten = 0;
    }

    private void meditate() throws InterruptedException {
        postMsg("meditating for " + state.getMeditateTime());
            Thread.sleep(state.getMeditateTime());
    }

    private void postMsg(String str) {
        System.out.printf("Time: %d Event: %d Philosopher %d %s " + state.name() + "\n",
                System.currentTimeMillis(), ++event, id, str);
    }

    private void takeSeat() throws InterruptedException{
        seat = table.takeSeat();
    }

    private void sleepBan() throws InterruptedException{
        postMsg("I have been banned!!!!!");
        sleep(5);
    }
}