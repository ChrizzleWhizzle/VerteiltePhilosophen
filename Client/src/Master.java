import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Master extends Thread {

    private static int event = 0;
    private Table _table;
    //int minEaten = Integer.MAX_VALUE;
    //int maxEaten;
    int _difference;
    private List<Philosopher> _phils;
    private boolean _feeding = false;


    public Master(Table t, int difference) {
        _table = t;
        _table.addMaster(this);
        _phils = new CopyOnWriteArrayList<>();
        _difference = difference;
    }


    public void addPhilosophers(int nNormalPhils, int nHungryPhils) {
        int totalPhils = nNormalPhils + nHungryPhils;

        // check if at least on phil will be added
        if (totalPhils < 1) {
            return;
        }

        for (int i = 0; i < totalPhils; i++) {
            Philosopher p;
            if (nHungryPhils > 0) {
                p = new Philosopher(i + 1, _table, PhilosopherState.HUNGRY);
                nHungryPhils--;
            } else {
                p = new Philosopher(i + 1, _table, PhilosopherState.NORMAL);
            }
            _phils.add(p);

            if (_feeding) p.start();
        }

        postMsg(String.format("Table: %s | #Philosophers added: %d", _table.getName(), totalPhils));

    }

    public void addSeats(int nSeatsToBeAdded) {
        _table.addSeats(nSeatsToBeAdded);
    }

    public Table getTable() {
        return _table;
    }

    public boolean isAllowedToEat(Philosopher phil) {
        int minEaten = Integer.MAX_VALUE;

        boolean allowedToEat = true;
        for (Philosopher p : _phils) {
            if (p == phil) continue;
            minEaten = Math.min(minEaten, p.totalMealsEaten);
            if (phil.totalMealsEaten > (minEaten + _difference)) {
                allowedToEat = false;
                break;
            }
        }

        return allowedToEat;
    }

    public boolean removeSeats(int nSeatsToBeDeleted) {
        _table.removeSeats(nSeatsToBeDeleted);
        return true;
    }


    /*    @Override
        public void run() {
            try {
                //sleep(100);
                while (!Thread.currentThread().isInterrupted()) {
                    minEaten = Integer.MAX_VALUE;
                    maxEaten = 0;

                    for (Table t : _table.values()) {

                        Iterator<Philosopher> it1 = t.getPhilosophers().iterator();
                        while(it1.hasNext()) {
                            Philosopher p = it1.next();
                            minEaten = Math.min(minEaten, p.totalMealsEaten);
                            if (p.totalMealsEaten >= (minEaten + _difference)) {
                                p.state.setBanned(true);
                            } else {
                                p.state.setBanned(false);
                                //System.out.println("notify phil " + p.toString());
                            }
                            maxEaten = Math.max(maxEaten, p.totalMealsEaten);
                        }
                        /*for (Philosopher p : t.getPhilosophers()) {
                            minEaten = Math.min(minEaten, p.totalMealsEaten);
                            if (p.totalMealsEaten >= (minEaten + _difference)) {
                                p.state.setBanned(true);
                            } else {
                                p.state.setBanned(false);
                                //System.out.println("notify phil " + p.toString());
                            }
                            maxEaten = Math.max(maxEaten, p.totalMealsEaten);
                        }
                    }
                    minEaten = maxEaten;
                }
            } catch (Exception e) {
                return;
            }
        }

    */
    public void startTheFeeding() {
        System.out.println("Start the feeding");
        _feeding = true;
        _phils.forEach(p -> p.start()); //Thread::start);
    }

    public void stopTheFeeding() {
        System.out.println("Stop the Feeding");
        _feeding = false;
        _phils.forEach(Thread::interrupt);
    }

    /**
     * @param compareToThisSeat Compare seats from other tables to this
     * @return
     */
    public Seat takeSeat(Seat compareToThisSeat) throws InterruptedException {

        // TODO: ask the servermaster for better free seats

        // get seat from next table
        Seat s = _table.takeSeat(true);

        if (s.lock.getQueueLength() < compareToThisSeat.lock.getQueueLength()) {
            // set compareseat to get minimum queue length
            compareToThisSeat = s;
        }
        return compareToThisSeat;
    }

    private void postMsg(String str) {
        System.out.printf("Time: %d Event: %d Tablemaster %s \n",
                System.currentTimeMillis(), ++event, str);
    }

    public List<Philosopher> getPhilosophers() {
        return _phils;
    }
}
