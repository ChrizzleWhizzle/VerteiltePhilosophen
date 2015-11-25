import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class Table {

    private List<Seat> seats;
    private List<Fork> forks;
    private Master master;
    private int seatSize;
    private static final int MIN_SEATS = 2;

    public Table(int nSeatCount) throws IllegalArgumentException {
        this.seats = new ArrayList<>();
        this.forks = new ArrayList<>();
        if (nSeatCount < MIN_SEATS) throw new IllegalArgumentException("Table must have at least " + MIN_SEATS + " Seats!");

        addSeats(nSeatCount);
    }

    public void addMaster(Master m) { this.master = m; }

    public Master getMaster() {
        return master;
    }

    public void addSeats(int nSeatCount) {

        int nForks = nSeatCount;
        int nSeats = nSeatCount;

        List<Seat> seatList = new ArrayList<>(nSeats);
        List<Fork> forkList = new ArrayList<>(nForks);

        //Create forks
        for(int i = 0; i < nForks; i++){
            try {
                forkList.add(new Fork());
            } catch (RemoteException e) {
                // this exception will never be thrown
            }
        }

        //Create seats
        int seatID = 1;
        seatList.add(new Seat(seatID++, forkList.get(nForks - 1),forkList.get(0)));
        for(int i = 0; i < nSeats - 1; i++){
            seatList.add(new Seat(seatID++, forkList.get(i),forkList.get(i+1)));
        }

        this.seats.addAll(seatList);
        this.seatSize = this.seats.size();
        this.forks.addAll(forkList);
    }

    public Seat takeSeat() throws  InterruptedException{
        Seat freeSeat = seats.get(0);
        try{
            for(int i = 0; i < seats.size(); i++){ //todo reihenfolge zufällig modulo
                Seat currentSeat = seats.get(i);
                // If current seat is free, check left seat then right seat(add seatsize to avoid -1 return.
                if(!currentSeat.lock.isLocked()
                        && !seats.get((i+1)%seatSize).lock.isLocked()
                        && !seats.get((i - 1 + seatSize) % seatSize).lock.isLocked()){
                freeSeat = currentSeat;
                    break;
                }
                if(freeSeat.lock.getQueueLength() > currentSeat.lock.getQueueLength()){
                    freeSeat = currentSeat;
                }
            }
            freeSeat.lock.lockInterruptibly();
        }
        finally {

        }
        return freeSeat;
    }
}
