import java.rmi.RemoteException;
import java.util.Scanner;

public class Main {

    private static Master _master;

    public static void main(String[] args) throws RemoteException {

        int nSeatCount = 2;
        int nPhilosophers = 2;
        int nHungryPhils = 1;
        String serverIP = "localhost";

        //Create table
        Table table;
        table = new Table("Tafelrunde", nSeatCount);

        _master = new Master(table, 10);
        _master.connectToServermasterAndAddOwnTable(serverIP);
        _master.addPhilosophers(nPhilosophers, nHungryPhils);


        //Create 2nd table
        Table table2;
        table2 = new Table("Das letzte Abendmahl", nSeatCount);

        Master m2 = new Master(table2, 10);
        m2.connectToServermasterAndAddOwnTable(serverIP);
        m2.addPhilosophers(nPhilosophers, nHungryPhils);

        System.out.println("Building some tension");

        try {
            for (int i = 0; i < 5; i++) {
                System.out.print(".");
                Thread.sleep(1000);
            }
            System.out.print("\n");
        } catch (InterruptedException e) {
        }


        _master.startTheFeeding();
        m2.startTheFeeding();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }


        try {
            _master.addSeats(3);
            m2.addSeats(10);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        _master.addPhilosophers(10, 10000);
        m2.addPhilosophers(10, 10);

        // try to remove more seats than possible
        _master.removeSeats(Integer.MAX_VALUE);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
        }

        _master.stopTheFeeding();
        m2.stopTheFeeding();

        System.out.println("Joining all Philosopher-Threads");
        long sumMealsEaten = 0;
        int minEaten = Integer.MAX_VALUE;
        int maxEaten = 0;

        for (Philosopher p : _master.getPhilosophers()) {
            try {
                p.join();
                System.out.println(p);
            } catch (InterruptedException e) {
            } finally {
                minEaten = Math.min(minEaten, p.totalMealsEaten);
                maxEaten = Math.max(maxEaten, p.totalMealsEaten);
                sumMealsEaten += p.totalMealsEaten;
            }
        }
        for (Philosopher p : m2.getPhilosophers()) {
            try {
                p.join();
                System.out.println(p);
            } catch (InterruptedException e) {
            } finally {
                minEaten = Math.min(minEaten, p.totalMealsEaten);
                maxEaten = Math.max(maxEaten, p.totalMealsEaten);
                sumMealsEaten += p.totalMealsEaten;
            }
        }


        System.out.println("Total Meals eaten: " + sumMealsEaten + " max: " + maxEaten + " min: " + minEaten);
        System.exit(0);
    }
}
