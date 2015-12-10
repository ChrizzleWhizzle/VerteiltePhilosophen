import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_ServerMaster extends Remote {

    boolean addTable(I_Table t) throws RemoteException;

    int getAllMinEaten(int m) throws RemoteException;

    I_Seat takeSeat(I_Table table, I_Seat compareToThisSeat) throws RemoteException;

}
