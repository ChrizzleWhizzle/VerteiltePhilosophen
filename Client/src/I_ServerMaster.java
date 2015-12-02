import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_ServerMaster extends Remote{

    boolean addTable(I_Table t)throws RemoteException;
}
