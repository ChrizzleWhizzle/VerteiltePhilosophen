import java.rmi.RemoteException;
import java.util.Scanner;

public class Main {

    private static Scanner _scanner = new Scanner(System.in);
    private static Master _master;

    public static void main(String[] args) throws RemoteException {

        //printMenu();

        int nSeatCount = 2;
        int nPhilosophers = 2;
        int nHungryPhils = 1;

        //Create table
        Table table;
        table = new Table("Tafelrunde", nSeatCount);

        _master = new Master(table, 10);
        _master.start();
        _master.addPhilosophers(nPhilosophers, nHungryPhils);

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


        _master.startTheFeeding();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }


        try {
            _master.addSeats(10);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        _master.addPhilosophers(10, 10);

        // try to remove more seats than possible
        _master.removeSeats(Integer.MAX_VALUE);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
        }

        _master.stopTheFeeding();

        _master.interrupt();
        System.out.println("Joining all Philosopher-Threads");
        long sumMealsEaten = 0;
        int minEaten = Integer.MAX_VALUE;
        int maxEaten = 0;

            for (Philosopher p : _master.getPhilosophers()) {
                try {
                    p.join();
                    System.out.println(p.toString() + p.totalMealsEaten);
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

    private static void printMenu() {

        System.out.println("-- Menu --");
        System.out.println("1. Init Table. ");
        //if (_table != null) {

            System.out.println("2. Add Seats to Table. ");
            System.out.println("3. Exit the program. ");

        //}

        System.out.println("Please select one of the above options:");

        String input = _scanner.nextLine();
        switch (input.toLowerCase()) //This way it doesn't matter if they typed an uppercase letter
        {
            case "1":
                System.out.println("You have selected option X");
                break;
            case "2":
                System.out.println("You have selected option Y");
                break;
            case "3":
                System.out.println("You have selected option Z");
                break;
            default:
                System.out.println("You entered an invalid option");
                break;
        }
    }

    private static void m_InitTable() {
        System.out.println("-- Init Table --");
        System.out.println("Please enter a name for the Table");

        String input = _scanner.nextLine();
        switch (input.toLowerCase()) //This way it doesn't matter if they typed an uppercase letter
        {
            case "":
                System.out.println("Sorry you entered no name");
                m_InitTable();
            default:
                break;
        }

        int numberOfSeats = _scanner.nextInt();
        switch (numberOfSeats) //This way it doesn't matter if they typed an uppercase letter
        {
            case 0:
                System.out.println("Sorry you entered no name");
                m_InitTable();
            default:
                break;
        }
    }

    private void m_AddSeats() {

    }
}
