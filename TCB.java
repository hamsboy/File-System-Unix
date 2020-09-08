/*
Hamidou Diallo
Jeramie Castillo
*/
public class TCB {
    private Thread thread = null;
    private int tid = 0;
    private int pid = 0;
    private boolean terminated = false;
    private int sleepTime = 0;
    public FileTableEntry[] ftEnt = null; 

    public TCB( Thread newThread, int myTid, int parentTid ) {
	thread = newThread;
	tid = myTid;
	pid = parentTid;
	terminated = false;

	ftEnt = new FileTableEntry[32];    

	System.err.println( "threadOS: a new thread (thread=" + thread + 
			    " tid=" + tid + 
			    " pid=" + pid + ")");
    }

    public synchronized Thread getThread( ) {
	return thread;
    }

    public synchronized int getTid( ) {
	return tid;
    }

    public synchronized int getPid( ) {
	return pid;
    }

    public synchronized boolean setTerminated( ) {
	terminated = true;
	return terminated;
    }

    public synchronized boolean getTerminated( ) {
	return terminated;
    }

 
    public synchronized int getFd( FileTableEntry entry ) {
	if ( entry == null )
	    return -1;
	for ( int i = 3; i < 32; i++ ) {
	    if ( ftEnt[i] == null ) {
		ftEnt[i] = entry;
		return i;
	    }
	}
	return -1;
    }

 
    public synchronized FileTableEntry returnFd( int fd ) {
	if ( fd >= 3 && fd < 32 ) {
	    FileTableEntry oldEnt = ftEnt[fd];
	    ftEnt[fd] = null;
	    return oldEnt;
	}
	else
	    return null;
    }

 
    public synchronized FileTableEntry getFtEnt( int fd ) {
	if ( fd >= 3 && fd < 32 )
	    return ftEnt[fd];
	else
	    return null;
    }
}