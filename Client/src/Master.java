import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Master extends UnicastRemoteObject
        implements I_Master {

    private static int event = 0;
    private Table _table;
    //int minEaten = Integer.MAX_VALUE;
    //int maxEaten;
    int _difference;
    private List<Philosopher> _phils;
    private boolean _feeding = false;
    private I_ServerMaster _sm;


    public Master(Table t, int difference) throws RemoteException {
        _table = t;
        _table.addMaster(this);
        _phils = new CopyOnWriteArrayList<>();
        _difference = difference;
    }

    @Override
    public void connectToServermasterAndAddOwnTable(String serverMasterIP) throws RemoteException {
        try {
            _sm = (I_ServerMaster) Naming.lookup("//" + serverMasterIP + ":2020/ServerMaster");

            if (_sm.addTable(_table)) {
                postMsg("Added Table successfully");
            }else {
                postMsg("Table couldn't be added or was already in list of servermaster");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addPhilosophers(int nNormalPhils, int nHungryPhils)  throws RemoteException{
        int totalPhils = nNormalPhils + nHungryPhils;

        // check if at least on phil will be added
        if (totalPhils < 1) {
            return;
        }

        for (int i = 0; i < totalPhils; i++) {
            PhilosopherState state;
            if (nHungryPhils > 0) {
                state = PhilosopherState.HUNGRY;
                nHungryPhils--;
            } else {
                state = PhilosopherState.NORMAL;
            }
            Philosopher p = new Philosopher(_phils.size()+1, _table, state, _table.getMaxMealsEaten());

            _phils.add(p);

            if (_feeding) p.start();
        }

        postMsg(String.format("Table: %s | #Philosophers added: %d", _table.getName(), totalPhils));

    }

    @Override
    public void addSeats(int nSeatsToBeAdded) throws RemoteException  {
        _table.addSeats(nSeatsToBeAdded);
    }

    @Override
    public Table getTable() throws RemoteException {
        return _table;
    }

    @Override
    public boolean isAllowedToEat(Philosopher phil) throws RemoteException {
        int minEaten = _sm.getAllMinEaten(_table.getMaxMealsEaten());
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

    @Override
    public boolean removeSeats(int nSeatsToBeDeleted)  throws RemoteException{
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
    @Override
    public void startTheFeeding() throws RemoteException {
        System.out.println("Start the feeding");
        if (_feeding) return;
        _feeding = true;
        _phils.forEach(p -> p.start()); //Thread::start);
    }

    @Override
    public void stopTheFeeding() throws RemoteException{
        System.out.println("Stop the Feeding");
        _feeding = false;
        _phils.forEach(Thread::interrupt);
    }

    /**
     * @param compareToThisSeat Compare seats from other tables to this
     * @return
     */
    @Override
    public Seat takeSeat(Seat compareToThisSeat) throws InterruptedException, RemoteException {

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

    @Override
    public List<Philosopher> getPhilosophers()  throws RemoteException {
        return _phils;
    }
}
