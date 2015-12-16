import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerMaster extends UnicastRemoteObject implements I_ServerMaster {

    private static int event = 0;
    private List<I_Table> _tableList;

    public ServerMaster() throws RemoteException {
        _tableList = new CopyOnWriteArrayList<>();
        try {
            LocateRegistry.createRegistry(2020);
            Naming.bind("//localhost:2020/ServerMaster",
                    this);
            postMsg("ServerMaster: finished rmi binding");
        } catch (Exception e) {
            // Registrieren des Remote-Objects fehlgeschlagen
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws RemoteException {
        ServerMaster sm = new ServerMaster();
    }

    /**
     * @param table Table to be added
     * @return false if the table is already in the map of the master
     */
    @Override
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

        if(_tableList.size() > 1){
            I_Table lastTable = _tableList.get(_tableList.size() - 2);
            table.connectWithOtherTable(_tableList.get(0));
            lastTable.connectWithOtherTable(table);
         }


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

    @Override
    public I_Seat takeSeat(I_Table table, I_Seat compareToThisSeat) throws RemoteException {
        I_Seat seatOfOtherTable = compareToThisSeat;
        I_Seat tmp;
        for(I_Table t: _tableList){
            if(t.equals(table)){
                continue;
            }
            try {
                tmp = t.takeSeat(true);
                postMsg("tmp seat: " + tmp);

                if (tmp.getLock().getQueueLength() < seatOfOtherTable.getLock().getQueueLength()) {
                    // set compareseat to get minimum queue length
                    postMsg("tmp seat is better than current held seat");
                    seatOfOtherTable = tmp;
                }
            }
            catch (InterruptedException e) {
                postMsg("Can't reach table " + t.getName());
            }
        }
        if (!compareToThisSeat.equals(seatOfOtherTable)){
            postMsg("Using seat from different table.");
        }
        return seatOfOtherTable;
    }

    private void postMsg(String str) {
        System.out.printf("Time: %d Event: %d ServerMaster %s \n",
                System.currentTimeMillis(), ++event, str);
    }

}
