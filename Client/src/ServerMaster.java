import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerMaster extends UnicastRemoteObject implements I_ServerMaster {

    private static int event = 0;
    private List<I_Table> _tableList;
    //int minEaten = Integer.MAX_VALUE;
    //int maxEaten;
    int _difference;

    public ServerMaster(int difference) throws RemoteException {
        _tableList = new CopyOnWriteArrayList<>();
        _difference = difference;
        try {
            LocateRegistry.createRegistry(2020);
            Naming.bind("//localhost:2020/ServerMaster",
                    this);
            System.out.println("ServerMaster: finished rmi binding");
        } catch (Exception e) {
            // Registrieren des Remote-Objects fehlgeschlagen
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws RemoteException {
        ServerMaster sm = new ServerMaster(10);
    }

   /*
    public void addSeats(Table t, int nSeatsToBeAdded) throws RemoteException {
        addSeats(t.getName(), nSeatsToBeAdded);
    }

    public void addSeats(String tableName, int nSeatsToBeAdded) throws RemoteException {
        I_Table t = _tableList.get(tableName);

        // table not found
        if (t == null) {
            return;
        }

        t.addSeats(nSeatsToBeAdded);
    }
*/

    /**
     * @param table Table to be added
     * @return false if the table is already in the map of the master
     */
    public boolean addTable(I_Table table) throws RemoteException {
        String tableName = table.getName();
        postMsg("Trying to add table." + tableName);

        for (I_Table t : _tableList) {
            try {
                if (t.getName().equals(tableName)) {
                    return false;
                }
            } catch (Exception e) {
                _tableList.remove(t);
                _tableList.add(table);
                postMsg("Table replaced." + tableName);
                return true;
            }
        }
        _tableList.add(table);
        postMsg("Table added." + tableName);
        return true;
    }

    @Override
    public int getAllMinEaten(int m) throws RemoteException {
        int result = m;
        for (I_Table t : _tableList) {
            result = Math.min(result, t.getMaxMealsEaten());
        }
        return result;
    }

    public List getTables() throws RemoteException {
        return _tableList;
    }

    public boolean isAllowedToEat(Philosopher phil) throws RemoteException {
        int minEaten = Integer.MAX_VALUE;
        int maxEaten = 0;

        boolean allowedToEat = true;
        /*for(Philosopher p: philList){
            if (p == phil) continue;
            minEaten = Math.min(minEaten,p.totalMealsEaten);
            if(phil.totalMealsEaten > (minEaten + difference)){
                allowedToEat = false;
                break;
            }
            //maxEaten = Math.max(maxEaten, p.totalMealsEaten);
        }*/
        //minEaten = maxEaten;

        return allowedToEat;
    }
/*
    public boolean removeSeats(Table t, int nSeatsToBeDeleted) throws RemoteException {
        return removeSeats(t.getName(), nSeatsToBeDeleted);
    }

    public boolean removeSeats(String tableName, int nSeatsToBeDeleted) throws RemoteException {

        I_Table t = _tableList.get(tableName);
        if (t == null) {
            return false;
        }

        t.removeSeats(nSeatsToBeDeleted);
        return true;
    }
*/
    int minEaten = Integer.MAX_VALUE;
    int maxEaten = 0;

    /* @Override
     public void run() {


         /*try {
             //sleep(100);
             while (!Thread.currentThread().isInterrupted()) {
                 minEaten = Integer.MAX_VALUE;
                 maxEaten = 0;

                 for (Table t : _tableList.values()) {

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
                     for (Philosopher p : t.getPhilosophers()) {
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
        //_tableList.values().forEach(Table::startTheFeeding);
        //for (Table t : _tableList.values()) {
        //    t.startTheFeeding();
        //}
    }

    public void stopTheFeeding() {
        System.out.println("Stop the Feeding");
        //_tableList.values().forEach(Table::stopTheFeeding);
    }

    /**
     * @param table             Table that asks if other Tables have a freeSeat
     * @param compareToThisSeat Compare seats from other tables to this
     * @return
     */
    public Seat takeSeat(Table table, Seat compareToThisSeat) throws InterruptedException, RemoteException {
        for (I_Table t : _tableList) {
            // skip own table
            if (t.getName().equals(table.getName())) continue;

            // get seat from next table
            Seat s = t.takeSeat(true);

            if (s.lock.getQueueLength() < compareToThisSeat.lock.getQueueLength()) {
                // set compareseat to get minimum queue length
                compareToThisSeat = s;
            }
        }
        return compareToThisSeat;
    }

    private void postMsg(String str) {
        System.out.printf("Time: %d Event: %d ServerMaster %s \n",
                System.currentTimeMillis(), ++event, str);
    }

}
