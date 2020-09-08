/*
Hamidou Diallo
Jeramie Castillo
*/
public class SyncQueue {
    private QueueNode queue[] = null;
    private final int COND_MAX = 10;
    private final int NO_PID = -1;

    private void initQueue(int condMax) {
        queue = new QueueNode[condMax];
        for ( int i = 0; i < condMax; i++ )
            queue[i] = new QueueNode();
    }

    public SyncQueue() {
        initQueue(COND_MAX);
    }

    public SyncQueue(int condMax) {
        initQueue(condMax);
    }

    int enqueueAndSleep(int pid) {
       

        if (pid >= 0 && pid < queue.length)
            return queue[pid].sleep();
        else
            return NO_PID;
    }

    void dequeueAndWakeup(int pid, int tid) {  
        if (pid >= 0 && pid < queue.length)
            queue[pid].wakeup(tid);
    }

    void dequeueAndWakeup(int pid) {
        dequeueAndWakeup(pid, 0); 
    }
}