import java.util.List;


public class Table {
    final List<Seat> seats;
    final List<Fork> forks;
    Master master;
    final int seatSize;

    public Table(List seats, List forks) {
        this.seats = seats;
        this.forks = forks;
        seatSize = seats.size();
    }
    public void addMaster(Master m) { this.master = m; }

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
