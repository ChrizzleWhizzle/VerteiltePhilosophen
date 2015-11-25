import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;

public class Master extends Thread{
    List<Philosopher> philList;
    int minEaten = Integer.MAX_VALUE;
    int maxEaten;
    int difference;

    public Master(List<Philosopher> philList, int difference) {
        this.philList = philList;
        this.difference = difference;
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
}
