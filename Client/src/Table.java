import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class Table {

    private List<Seat> _seats;
    private List<Fork> _forks;
    private Master master;
    private int _seatSize;
    private static final int MIN_SEATS = 2;
    private final String _name;
    private static int event = 0;

    public Table(String name, int nSeatCount) throws IllegalArgumentException {
        _name = name;
        _seats = new ArrayList<>();
        _forks = new ArrayList<>();
        if (nSeatCount < MIN_SEATS)
            throw new IllegalArgumentException("Table must have at least " + MIN_SEATS + " Seats!");

        addSeats(nSeatCount);
    }

    public void addMaster(Master m) {
        this.master = m;
    }

    public Master getMaster() {
        return master;
    }

    public void addSeats(int nSeatCount) {

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
        Fork lastRightFork = null;
        // first check if there already exists some seats, take the last seat and the right fork from the seat
        if (!_seats.isEmpty()) {
            lastSeat = _seats.get(_seatSize - 1);
            seatID = lastSeat.id+1;
            lastRightFork = lastSeat.getRightFork();
        }

        // last right fork is null if none seats are added yet to the table
        if (lastRightFork == null) {
            lastRightFork = forkList.get(0);
        }

        // first add one seat and take right fork from the last seat of the existing seats as left fork
        for (int i = 0; i < nSeats; i++) {
            seatList.add(new Seat(seatID++, forkList.get(i), forkList.get(i + 1)));
        }

        // last seat is null if no seats are yet created on the table
        if (lastSeat == null) {
            lastSeat = seatList.get(seatList.size() - 1);

            // we need to rebind the right fork of the last existing seat to our newly created first fork
            lastSeat.rebindRightFork(lastRightFork);
        } else {
            // first we need to ensure that the seat will not be taken by a new philosopher
            lastSeat.freeForSeatChoice = false;

            // wait if some philosophers are waiting to eat on that seat
            while (lastSeat.lock.getQueueLength() > 0 || lastSeat.lock.isLocked()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // nothing happens in here
                }
            }
            lastSeat.rebindRightFork(forkList.get(0));

            // we need to set the right fork of our last created seat to the previous right fork of the already existing last seat
            seatList.get(seatList.size() - 1).rebindRightFork(lastRightFork);
        }

        _seats.addAll(seatList);
        _seatSize = _seats.size();
        _forks.addAll(forkList);

        postMsg(String.format("#Seats added: %d | #Total seats: %d", nSeatCount, _seatSize));
    }

    public void connectWithOtherTable(Table t) {

    }

    /**
     * Get first fork from this Table (most left sided) --> needed for the next Table as it is used there for last right fork
     * Can't be null as a Table has at least two or more seats
     *
     * @return returns the first fork of the Table
     */
    public Fork getFirstFork() {
        return _forks.get(0);
    }

    public void removeSeats(int nSeatCount) {

        // we have to delete the seats from end to front of the list as we need to keep the most left fork

        // first get the last fork
        Fork mostRightFork = _seats.get(_seatSize - 1).getRightFork();

        // ensure that at least min seats will be kept
        int maxSeatsToDelete = Math.min(_seatSize - MIN_SEATS, nSeatCount);
        int deletedSeats = 0;

        for (int i = 0; i < maxSeatsToDelete; i++) {
            // first get the last and second last seat
            Seat lastSeat = _seats.get(_seatSize - 1);
            Seat secondLastSeat = _seats.get(_seatSize - 2);

            // disable seats to be selected on 'takeSeat' alogrithm
            lastSeat.freeForSeatChoice = false;
            secondLastSeat.freeForSeatChoice = false;

            // wait till every waiting/eating Philosopher leaves the seat
            while (lastSeat.lock.getQueueLength() > 0 || secondLastSeat.lock.getQueueLength() > 0 ||
                    lastSeat.lock.isLocked() || secondLastSeat.lock.isLocked()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // nothing happens in here
                }
            }

            // rebind the right fork of the second last seat to the fork of the last seat
            secondLastSeat.rebindRightFork(lastSeat.getRightFork());

            // if seat will be deleted next iteration we don't need to set him free again for seat scheduling
            if (i+1 == maxSeatsToDelete) {
                secondLastSeat.freeForSeatChoice = true;
            }
            // remove the last seat
            _seats.remove(lastSeat);

            // decrease available seatsize
            _seatSize--;

            deletedSeats++;
        }

        postMsg(String.format("#Seats to be deleted: %d | Actual deleted: %d | Seats left: %d", nSeatCount, deletedSeats, _seatSize));
    }

    public Seat takeSeat() throws InterruptedException {
        Seat freeSeat = _seats.get(0);
        try {
            for (int i = 0; i < _seats.size(); i++) { //todo reihenfolge zufällig modulo
                Seat currentSeat = _seats.get(i);

                // check if seat is still available for selection, may be not if seat or right neighbor will be deleted
                if (currentSeat.freeForSeatChoice) {
                    // If current seat is free, check left seat then right seat(add seatsize to avoid -1 return.
                    if (!currentSeat.lock.isLocked()
                            && !_seats.get((i + 1) % _seatSize).lock.isLocked()
                            && !_seats.get((i - 1 + _seatSize) % _seatSize).lock.isLocked()) {
                        freeSeat = currentSeat;
                        break;
                    }
                    if (freeSeat.lock.getQueueLength() > currentSeat.lock.getQueueLength()) {
                        freeSeat = currentSeat;
                    }
                }
            }
            freeSeat.lock.lockInterruptibly();
        } finally {

        }
        return freeSeat;
    }


    private void postMsg(String str) {
        System.out.printf("Time: %d Event: %d Table %s %s \n",
                System.currentTimeMillis(), ++event, _name, str);
    }
}
