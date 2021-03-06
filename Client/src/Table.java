import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;


public class Table extends UnicastRemoteObject
        implements I_Table {

    private List<Seat> _seats;
    private List<Fork> _forks;
    private Master master;
    private int _seatSize;
    private static final int MIN_SEATS = 2;
    private final String _name;
    private static int event = 0;
    private static final int checkOtherTablesIfQueuelengthIsBiggerThan = 5;
    private int maxMealsEaten = 0;


    public Table(String name, int nSeatCount) throws IllegalArgumentException, RemoteException  {
        _name = name;
        _seats = new CopyOnWriteArrayList<>();
        _forks = new CopyOnWriteArrayList<>();
        if (nSeatCount < MIN_SEATS)
            throw new IllegalArgumentException("Table must have at least " + MIN_SEATS + " Seats!");

        addSeats(nSeatCount);
    }

    public void addMaster(Master m) throws RemoteException {
        this.master = m;
    }

    public I_Master getMaster() throws RemoteException{
        return master;
    }

    public boolean isAllowedToEat(Philosopher p)  throws RemoteException{
        return master.isAllowedToEat(p);
    }

    public void addSeats(int nSeatCount) throws RemoteException {

        int nForks = nSeatCount + 1;
        int nSeats = nSeatCount;

        List<Seat> seatList = new ArrayList<>(nSeats);
        List<Fork> forkList = new ArrayList<>(nForks);

        //Create forks
        for (int i = 0; i < nForks; i++) {
            try {
                forkList.add(new Fork());
            } catch (RemoteException e) {
                // this exception will never be thrown
            }
        }

        //Create seats
        int seatID = 1;

        // |O|O|O|O (last right fork will be taken from first seat
        //  0 1 2 3
        Seat lastSeat = null;
        I_Fork lastRightFork = null;
        // first check if there already exists some seats, take the last seat and the right fork from the seat
        if (!_seats.isEmpty()) {
            lastSeat = _seats.get(_seatSize - 1);
            seatID = lastSeat.id + 1;
            lastRightFork = lastSeat.getRightFork();
        }

        // last right fork is null if none seats are added yet to the table
        if (lastRightFork == null) {
            lastRightFork = forkList.get(0);
        }

        // first add one seat and take right fork from the last seat of the existing seats as left fork
        for (int i = 0; i < nSeats; i++) {
            try {
                seatList.add(new Seat(seatID++, forkList.get(i), forkList.get(i + 1)));
            } catch (RemoteException e) {

            }
        }

        // last seat is null if no seats are yet created on the table
        if (lastSeat == null) {
            lastSeat = seatList.get(seatList.size() - 1);

            // we need to rebind the right fork of the last existing seat to our newly created first fork
            lastSeat.rebindRightFork(lastRightFork);
        } else {

            lastSeat.rebindRightFork(forkList.get(0));

            // we need to set the right fork of our last created seat to the previous right fork of the already existing last seat
            seatList.get(seatList.size() - 1).rebindRightFork(lastRightFork);
        }

        _seats.addAll(seatList);
        _seatSize = _seats.size();
        _forks.addAll(forkList);

        postMsg(String.format("#Seats added: %d | #Total seats: %d", nSeatCount, _seatSize));
    }

    public void connectWithOtherTable(I_Table otherTable)  throws RemoteException{
        I_Fork fstForkOtherTable = otherTable.getFirstFork();
        Seat lastSeat = _seats.get(_seats.size()-1);

        lastSeat.rebindRightFork(fstForkOtherTable);

    }

    /**
     * Get first fork from this Table (most left sided) --> needed for the next Table as it is used there for last right fork
     * Can't be null as a Table has at least two or more seats
     *
     * @return returns the first fork of the Table
     */
    public I_Fork getFirstFork()  throws RemoteException{
        return _forks.get(0);
    }

    public int getQueueLength() throws RemoteException {
        int queue = 0;

        for (Seat s : _seats) {
            queue += s.getLock().getQueueLength();
        }

        return queue;
    }

    @Override
    public void removeSeats(int nSeatCount)  throws RemoteException{

        // we have to delete the seats from end to front of the list as we need to keep the most left fork

        // ensure that at least min seats will be kept
        int maxSeatsToDelete = Math.min(_seatSize - MIN_SEATS, nSeatCount);
        int deletedSeats = 0;

        for (int i = 0; i < maxSeatsToDelete; i++) {
            // first get the last and second last seat
            Seat lastSeat = _seats.get(_seatSize - 1);
            Seat secondLastSeat = _seats.get(_seatSize - 2);

            // disable seats to be selected on 'takeSeat' alogrithm
            lastSeat.setFreeForSeatChoice(false);
            secondLastSeat.setFreeForSeatChoice(false);

            // wait till every waiting/eating Philosopher leaves the seat
            while (lastSeat.getLock().getQueueLength() > 0 || secondLastSeat.getLock().getQueueLength() > 0 ||
                    lastSeat.getLock().isLocked() || secondLastSeat.getLock().isLocked()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // nothing happens in here
                }
            }

            // rebind the right fork of the second last seat to the fork of the last seat
            secondLastSeat.rebindRightFork(lastSeat.getRightFork());

            // if seat will be deleted next iteration we don't need to set him free again for seat scheduling
            if (i + 1 == maxSeatsToDelete) {
                secondLastSeat.setFreeForSeatChoice(true);
            }
            // remove the last seat
            _seats.remove(lastSeat);

            // decrease available seatsize
            _seatSize--;

            deletedSeats++;
        }

        postMsg(String.format("#Seats to be deleted: %d | Actual deleted: %d | Seats left: %d", nSeatCount, deletedSeats, _seatSize));
    }


    public I_Seat takeSeat(boolean tableMasterIsAsking) throws InterruptedException, RemoteException  {
        I_Seat freeSeat = _seats.get(0);
        boolean freeSeatHasPhilsWaiting = false;
        //try {
        for (int i = new Random().nextInt(_seatSize); i < _seats.size(); i++) {
            Seat currentSeat = _seats.get(i);

            // check if seat is still available for selection, may be not if seat or right neighbor will be deleted
            if (currentSeat.isFreeForSeatChoice()) {
                // If current seat is free, check left seat then right seat(add seatsize to avoid -1 return.
                if (!currentSeat.getLock().isLocked()
                        && !_seats.get((i + 1) % _seatSize).getLock().isLocked()
                        && !_seats.get((i - 1 + _seatSize) % _seatSize).getLock().isLocked()) {
                    freeSeat = currentSeat;
                    break;
                }
                if (freeSeat.getLock().getQueueLength() > currentSeat.getLock().getQueueLength()) {
                    freeSeat = currentSeat;
                }
            }
        }
        if (freeSeat.getLock().getQueueLength() > checkOtherTablesIfQueuelengthIsBiggerThan) {
            freeSeatHasPhilsWaiting = true;
        }

        // check other tables for a seat to sit directly, skip if tablemaster called this method to prevent recursive stack overflow
        if (freeSeatHasPhilsWaiting && !tableMasterIsAsking) {
            freeSeat = master.takeSeat(freeSeat);
        }
        // try to lock the seat in the philosopher thread

        return freeSeat;
    }
    public void setMaxEatenIfMore(int m){
        synchronized (this){
            maxMealsEaten = Math.max(maxMealsEaten,m);
        }
    }
    public int getMaxMealsEaten() throws RemoteException{
        return maxMealsEaten;
    }



    private void postMsg(String str) {
        System.out.printf("Time: %d Event: %d Table %s %s \n",
                System.currentTimeMillis(), ++event, _name, str);
    }

    public String getName() throws RemoteException {
        return _name;
    }

    @Override
    public String toString() {
        return _name;
    }
}
