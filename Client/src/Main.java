import javafx.scene.paint.PhongMaterial;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args){
        int nForks = 5;
        int nSeats = 5;
        int nPhilosophers = 5;
        int nHungryPhils = 2;
        List<Seat> seatList = new ArrayList<>(nSeats);
        List<Fork> forkList = new ArrayList<>(nForks);
        List<Philosopher> philList = new ArrayList<>(nPhilosophers);

        //Create forks
        for(int i = 0; i < nForks; i++){
            forkList.add(new Fork());
        }

        //Create seats
        int seatID = 1;
        seatList.add(new Seat(seatID++, forkList.get(nForks - 1),forkList.get(0)));
        for(int i = 0; i < nSeats - 1; i++){
            seatList.add(new Seat(seatID++, forkList.get(i),forkList.get(i+1)));
        }
        System.out.println("Seatlist: " + seatList.toString());
        System.out.println("Forklist: " + forkList.toString());

        //Create table
        Table table = new Table(seatList,forkList);
        for(int i = 0; i < nPhilosophers; i++){
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
        Master master = new Master(philList, 10);
        table.addMaster(master);

        philList.forEach(p -> p.start());
        //master.start();
        System.out.println("started");

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }

        System.out.println("Killing all Philosophers");

        philList.forEach(p -> p.interrupt());
        master.interrupt();
        System.out.println("Joining all Philosopher-Threads");
        long sumMealsEaten = 0;
        int minEaten = Integer.MAX_VALUE;
        int maxEaten = 0;
        for (Philosopher p : philList) {
            try {

                p.join();
                System.out.println(p.toString() + p.totalMealsEaten);
            } catch (InterruptedException e) {
            }finally {
                minEaten = Math.min(minEaten,p.totalMealsEaten);
                maxEaten = Math.max(maxEaten,p.totalMealsEaten);
                sumMealsEaten += p.totalMealsEaten;
            }
        }

        System.out.println("Total Meals eaten: " + sumMealsEaten + " max: " + maxEaten + " min: "+ minEaten);
    }
}
