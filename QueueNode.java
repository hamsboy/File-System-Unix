/*
Hamidou Diallo
Jeramie Castillo
*/
import java.util.Vector;
public class QueueNode {
    private Vector<Integer> tids;

    public QueueNode()
    {
        tids = new Vector<Integer>();
    }

    public synchronized int sleep()
    {
       
        if (size() == 0)
        {
            try
            {
                wait(); 
            }
            catch (InterruptedException e){}  

            return tids.remove(0); 
        }
     
        return -1;
    }

    public synchronized void wakeup(int tid)
    {
        
        tids.add(tid);
        notify();
    }

  
    public synchronized int size()
    {
        return tids.size();
    }
}