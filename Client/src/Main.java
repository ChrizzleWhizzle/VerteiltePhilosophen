import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws RemoteException {

        int nSeatCount = 2;
        int nPhilosophers = 2;
        int nHungryPhils = 1;

        //Create table
        Table table = new Table("Tafelrunde", nSeatCount);

        Master master = new Master(table, 10);
        master.addPhilosophers(nPhilosophers, nHungryPhils);
        table.addMaster(master);

        //master.start();
        System.out.println("Building some tension");

        try {
            for (int i = 0; i < 5; i++) {
                System.out.print(".");
                Thread.sleep(1000);
            }
            System.out.print("\n");
        } catch (InterruptedException e) {
        }


        master.startTheFeeding();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }


        try {
            System.out.println("Try to add ten seats for 5 seconds");
            master.addSeats(10);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

            System.out.println("Try to remove more seats than possible");
            // try to remove more seats than possible
            master.removeSeats(Integer.MAX_VALUE);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
        }

        master.stopTheFeeding();

        master.interrupt();
        System.out.println("Joining all Philosopher-Threads");
        long sumMealsEaten = 0;
        int minEaten = Integer.MAX_VALUE;
        int maxEaten = 0;
        for (Philosopher p : master.philList) {
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
