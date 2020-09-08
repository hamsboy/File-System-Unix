
/*
Hamidou Diallo
Jeramie Castillo
*/
import java.util.*;

public class Scheduler extends Thread
{
    private Vector queue;
    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 1000;

    
    private boolean[] tids; 
    private static final int DEFAULT_MAX_THREADS = 10000;

   
    private int nextId = 0;
    private void initTid( int maxThreads ) {
        tids = new boolean[maxThreads];
        for ( int i = 0; i < maxThreads; i++ )
            tids[i] = false;
    }

  
    private int getNewTid( ) {
        for ( int i = 0; i < tids.length; i++ ) {
            int tentative = ( nextId + i ) % tids.length;
            if ( tids[tentative] == false ) {
                tids[tentative] = true;
                nextId = ( tentative + 1 ) % tids.length;
                return tentative;
            }
        }
        return -1;
    }


    private boolean returnTid( int tid ) {
        if ( tid >= 0 && tid < tids.length && tids[tid] == true ) {
            tids[tid] = false;
            return true;
        }
        return false;
    }

  
    public TCB getMyTcb( ) {
        Thread myThread = Thread.currentThread( ); 
        synchronized( queue ) {
            for ( int i = 0; i < queue.size( ); i++ ) {
            TCB tcb = ( TCB )queue.elementAt( i );
            Thread thread = tcb.getThread( );
            if ( thread == myThread ) 
                return tcb;
            }
        }
        return null;
    }

    
    public int getMaxThreads( ) {
        return tids.length;
    }

    public Scheduler( ) {
        timeSlice = DEFAULT_TIME_SLICE;
        queue = new Vector( );
        initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler( int quantum ) {
        timeSlice = quantum;
        queue = new Vector( );
        initTid( DEFAULT_MAX_THREADS );
    }


    public Scheduler( int quantum, int maxThreads ) {
        timeSlice = quantum;
        queue = new Vector( );
        initTid( maxThreads );
    }

    private void schedulerSleep( ) {
        try {
            Thread.sleep( timeSlice );
        } catch ( InterruptedException e ) { }
    }


    public TCB addThread( Thread t ) {
        t.setPriority( 2 );
        TCB parentTcb = getMyTcb( ); 
        int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
        int tid = getNewTid( ); 
        if ( tid == -1)
            return null;
        TCB tcb = new TCB( t, tid, pid );
    
        if ( parentTcb != null ) {
            for ( int i = 0; i < 32; i++ ) {
                tcb.ftEnt[i] = parentTcb.ftEnt[i];
               
                if ( tcb.ftEnt[i] != null )
                    tcb.ftEnt[i].count++;
            }
        }
    
        queue.add( tcb );
        return tcb;
    }


    public boolean deleteThread( ) {
        TCB tcb = getMyTcb( ); 
        if ( tcb == null )
            return false;
        else {
           
            for ( int i = 3; i < 32; i++ )
                if ( tcb.ftEnt[i] != null )
                  
                    SysLib.close( i );
            return tcb.setTerminated( );
        }
    }

    public void sleepThread( int milliseconds ) {
        try {
            sleep( milliseconds );
        } catch ( InterruptedException e ) { }
    }
    
 
    public void run( ) {
        Thread current = null;
    
        this.setPriority( 6 );
        
        while ( true ) {
            try {
             
                if ( queue.size( ) == 0 )
                    continue;
                TCB currentTCB = (TCB)queue.firstElement( );
                if ( currentTCB.getTerminated( ) == true ) {
                    queue.remove( currentTCB );
                    returnTid( currentTCB.getTid( ) );
                    continue;
                }
                current = currentTCB.getThread( );
                if ( current != null ) {
                    if ( current.isAlive( ) )
                        current.setPriority( 4 );
                    else {
                       
                        current.start( ); 
                        current.setPriority( 4 );
                    }
                }
                
                schedulerSleep( );
                
                
                synchronized ( queue ) {
                    if ( current != null && current.isAlive( ) )
                        current.setPriority( 2 );
                    queue.remove( currentTCB ); 
                    queue.add( currentTCB );
                }
            } catch ( NullPointerException e3 ) { };
        }
    }
}