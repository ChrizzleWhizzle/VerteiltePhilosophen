import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;

public class Master extends Thread{

    private final Table table;
    List<Philosopher> philList;
    int minEaten = Integer.MAX_VALUE;
    int maxEaten;
    int difference;

    public Master(Table t, int difference) {
        this.table = t;
        this.philList = new ArrayList<>();
        this.difference = difference;
    }


    public void addPhilosophers(int nNormalPhils, int nHungryPhils) {
        int totalPhils = nNormalPhils + nHungryPhils;

        for(int i = 0; i < totalPhils; i++){
            Philosopher p;
            if(nHungryPhils > 0){
                p = new Philosopher(i + 1, table,PhilosopherState.HUNGRY);
                nHungryPhils--;
            }
            else {
                p = new Philosopher(i + 1, table,PhilosopherState.NORMAL);
            }
            philList.add(p);
        }
    }

    public void addSeats(int nSeatsToBeAdded) {
        table.addSeats(nSeatsToBeAdded);
    }

    public void removeSeats(int nSeatsToBeDeleted) {
        table.removeSeats(nSeatsToBeDeleted);
    }

    @Override
    public void run() {
        try {
            sleep(100);
            while (!Thread.currentThread().isInterrupted()) {
                maxEaten = 0;
                for(Philosopher p: philList){
                    minEaten = Math.min(minEaten,p.totalMealsEaten);
                    if(p.totalMealsEaten > (minEaten + difference)){
                        p.state.setBanned(true);
                    }
                    else {
                        p.state.setBanned(false);
                        //System.out.println("notify phil " + p.toString());
                    }
                    maxEaten = Math.max(maxEaten, p.totalMealsEaten);
                }
                minEaten = maxEaten;
            }
        } catch (Exception e) {
            return;
        }
    }

    public void startTheFeeding() {
        System.out.println("Start the feeding");

        philList.forEach(p -> p.start());
    }

    public boolean isAllowedToEat(Philosopher phil) {
        int minEaten = Integer.MAX_VALUE;
        int maxEaten = 0;

        boolean allowedToEat = true;
        for(Philosopher p: philList){
            if (p == phil) continue;
            minEaten = Math.min(minEaten,p.totalMealsEaten);
            if(phil.totalMealsEaten > (minEaten + difference)){
                allowedToEat = false;
                break;
            }
            //maxEaten = Math.max(maxEaten, p.totalMealsEaten);
        }
        //minEaten = maxEaten;

        return allowedToEat;
    }

    public void stopTheFeeding() {
        System.out.println("Stop the Feeding");
        philList.forEach(p -> p.interrupt());
    }
}
