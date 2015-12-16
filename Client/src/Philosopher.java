import java.rmi.RemoteException;

public class Philosopher extends Thread {
    // Process:
    // start with meditate
    // if hungry go to dining room
    // take a free seat or a seat with a small waiting queue
    // take right fork (only one Philosopher can take the fork at time)
    // take left fork (only one Philosopher can take the fork at time)
    // eat
    // go back to meditate
    // after 3 meals go to sleep
    // (rave)
    // repeat

    public PhilosopherState state;
    private static int event = 0;

    private int id;
    private I_Seat seat;
    private Table table;
    private int mealsEaten;
    public int totalMealsEaten;
    private boolean hasBothForks = false;
    private static final boolean DEBUG = false;

    public Philosopher(int id, Table table, PhilosopherState state, int mealsEatenOffset) {
        this.id = id;
        this.table = table;
        this.state = state;
        totalMealsEaten = mealsEatenOffset;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // ask table if allowed to eat
                if (table.isAllowedToEat(this)) {
                    takeSeat();
                    // take forks
                    while (!hasBothForks) {
                        if (seat.takeRightFork()) {
                            if (!seat.takeLeftFork()) {
                                seat.dropRight();
                            } else {
                                hasBothForks = true;
                            }
                        } else {
                            break;
                        }
                    }
                    // if philosopher couldn't get both forks after several tries, start from the beginning and get a new seat assigned.
                    if (!hasBothForks) {
                        System.out.println("Could not take forks");
                        //postMsg("Could not take forks");
                        seat.standUp();
                        continue;
                    }
                    eat();
                    table.setMaxEatenIfMore(totalMealsEaten);
                    seat.dropLeft();
                    seat.dropRight();
                    seat.standUp();
                    meditate();
                    if (mealsEaten == state.getMaxMealsEaten()) {
                        goToSleep();
                    }
                } else {
                    sleepBan();
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                seat.dropLeft();
                seat.dropRight();
                seat.standUp();
            } catch (RemoteException e) {

            }
        }
    }

    private void eat() throws InterruptedException, RemoteException {
        postMsg("eating for " + state.getEatTime() + "on Seat: " + seat.getId());
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
        if (DEBUG) {
            System.out.printf("Time: %d Event: %d T: %s Philosopher %d %s " + state.name() + "\n",
                    System.currentTimeMillis(), ++event, table, id, str);
        }
    }

    private void takeSeat() throws InterruptedException, RemoteException {
        seat = table.takeSeat(false);
        while (true ) {
            if (seat.sitDown()) return;
        }
    }

    private void sleepBan() throws InterruptedException {
        postMsg("I have been banned!!!!!");
        sleep(5);
    }

    @Override
    public String toString() {
        return String.format("T: %s Philosopher %d | Totalmeals: %d", table, id, totalMealsEaten);
    }
}