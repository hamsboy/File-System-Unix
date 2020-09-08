/*
Hamidou Diallo
Jeramie Castillo
*/
import java.util.*;
import java.lang.reflect.*;
import java.io.*;

public class Kernel extends Thread
{
   // Interrupt requests
   public final static int INTERRUPT_SOFTWARE = 1;  // System calls
   public final static int INTERRUPT_DISK     = 2;  // Disk interrupts
   public final static int INTERRUPT_IO       = 3;  // Other I/O interrupts

   // System calls
   public final static int BOOT    =  0; // SysLib.boot( )
   public final static int EXEC    =  1; // SysLib.exec(String args[])
   public final static int WAIT    =  2; // SysLib.join( )
   public final static int EXIT    =  3; // SysLib.exit( )
   public final static int SLEEP   =  4; // SysLib.sleep(int milliseconds)
   public final static int RAWREAD =  5; // SysLib.rawread(int blk, byte b[])
   public final static int RAWWRITE=  6; // SysLib.rawwrite(int blk, byte b[])
   public final static int SYNC    =  7; // SysLib.sync( )
   public final static int READ    =  8; // SysLib.cin( )
   public final static int WRITE   =  9; // SysLib.cout( ) and SysLib.cerr( )

   
   public final static int CREAD   = 10; 
   public final static int CWRITE  = 11; 
   public final static int CSYNC   = 12;
   public final static int CFLUSH  = 13; 

   
   public final static int OPEN    = 14; 
   public final static int CLOSE   = 15; 
   public final static int SIZE    = 16; 
   public final static int SEEK    = 17;
  
   public final static int FORMAT  = 18; 
   public final static int DELETE  = 19; 

   
   public final static int STDIN  = 0;
   public final static int STDOUT = 1;
   public final static int STDERR = 2;

 
   public final static int OK = 0;
   public final static int ERROR = -1;

   
   private static Scheduler scheduler;

   private static Disk disk;
   private static Cache cache;

   
   private static SyncQueue waitQueue;  
   private static SyncQueue ioQueue;   

   
   private static FileSystem fs;

   private final static int COND_DISK_REQ = 1; 
   private final static int COND_DISK_FIN = 2; 

   
   private static BufferedReader input= new BufferedReader( new InputStreamReader( System.in ) );

   
   public static int interrupt( int irq, int cmd, int param, Object args ) {
      TCB myTcb;
      switch( irq ) {
         case INTERRUPT_SOFTWARE: 
            switch( cmd ) { 
               case BOOT:
                
                  scheduler = new Scheduler( ); 
                  scheduler.start( );

                
                  disk = new Disk( 1000 );
                  disk.start( );

                  
                  cache = new Cache( disk.blockSize, 10 );

                  
                  ioQueue
                    = new SyncQueue( );
                  waitQueue = new SyncQueue( scheduler.getMaxThreads( ) );

                  
                  fs = new FileSystem( 1000 );

                  return OK;
               case EXEC:
                  return sysExec( ( String[] )args );
               case WAIT:
               try {
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     int myTid = myTcb.getTid( ); 
                     return waitQueue.enqueueAndSleep( myTid ); 
                     
                  }
                  return ERROR;
               } catch (Exception e) {
                  //TODO: handle exception
               }
                  
               case EXIT:
               try {
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     int myPid = myTcb.getPid( ); 
                     int myTid = myTcb.getTid( ); 
                     if ( myPid != -1 ) {
                       
                        waitQueue.dequeueAndWakeup( myPid, myTid );
                        // I'm terminated!
                        scheduler.deleteThread( );
                        return OK;
                     }
                  }
                  return ERROR;
               } catch (Exception e) {
                  //TODO: handle exception
               }
                 
               case SLEEP: 
                  try {
                     scheduler.sleepThread( param ); 
                     return OK;
                  } catch (Exception e) {
                     //TODO: handle exception
                  }  
                 
               case RAWREAD: 
                  try {
                     while ( disk.read( param, ( byte[] )args ) == false )
                     ioQueue.enqueueAndSleep( COND_DISK_REQ );
                  while ( disk.testAndResetReady( ) == false )
                     ioQueue.enqueueAndSleep( COND_DISK_FIN );
                  return OK;
                  } catch (Exception e) {
                     //TODO: handle exception
                  }
                 
               case RAWWRITE:
               try {
                  while ( disk.write( param, ( byte[] )args ) == false )
                  ioQueue.enqueueAndSleep( COND_DISK_REQ );
               while ( disk.testAndResetReady( ) == false )
                  ioQueue.enqueueAndSleep( COND_DISK_FIN );
               return OK;
               } catch (Exception e) {
                  //TODO: handle exception
               }
                 
               case SYNC: 
               try {
                  fs.sync( );
                  while ( disk.sync( ) == false )
                     ioQueue.enqueueAndSleep( COND_DISK_REQ );
                  while ( disk.testAndResetReady( ) == false )
                     ioQueue.enqueueAndSleep( COND_DISK_FIN );
                  return OK;
               } catch (Exception e) {
                  //TODO: handle exception
               }    
                 
               case READ:
                  switch ( param ) {
                     case STDIN:
                        try {
                           String s = input.readLine();
                           if ( s == null ) {
                              return ERROR;
                           }
                         
                           StringBuffer buf = ( StringBuffer )args;

                           
                           buf.append( s ); 

                           
                           return s.length( );
                        } catch ( IOException e ) {
                           System.out.println( e );
                           return ERROR;
                        }
                     case STDOUT:
                     case STDERR:
                        System.out.println( "threaOS: caused read errors" );
                        return ERROR;
                  }
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     FileTableEntry ftEnt = myTcb.getFtEnt( param );
                     if ( ftEnt != null )
                        return fs.read( ftEnt, ( byte[] )args );
                  }
                  return ERROR;
               case WRITE:
                  switch ( param ) {
                     case STDIN:
                        System.out.println( "threaOS: cannot write to System.in" );
                        return ERROR;
                     case STDOUT:
                        System.out.print( (String)args );
                        return OK;
                     case STDERR:
                        System.err.print( (String)args );
                        return OK;
                  }
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     FileTableEntry ftEnt = myTcb.getFtEnt( param );
                     if ( ftEnt != null )
                        return fs.write( ftEnt, ( byte[] )args );
                  }
                  return ERROR;
               case CREAD:
                  return cache.read( param, ( byte[] )args ) ? OK : ERROR;
               case CWRITE:
                  return cache.write( param, ( byte[] )args ) ? OK : ERROR;
               case CSYNC:
                  cache.sync( );
                  return OK;
               case CFLUSH:
                  cache.flush( );
                  return OK;
               case OPEN:
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     String[] s = ( String[] )args;
                     return myTcb.getFd( fs.open( s[0], s[1] ) );
                  } else
                     return ERROR;
               case CLOSE:
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     FileTableEntry ftEnt = myTcb.getFtEnt( param );
                     if ( ftEnt == null || fs.close( ftEnt ) == false )
                        return ERROR;
                     if ( myTcb.returnFd( param ) != ftEnt )
                        return ERROR;
                     return OK;
                  }
                  return ERROR;
               case SIZE:
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     FileTableEntry ftEnt = myTcb.getFtEnt( param );
                     if ( ftEnt != null )
                        return fs.fsize( ftEnt );
                  }
                  return ERROR;
               case SEEK:
                  if ( ( myTcb = scheduler.getMyTcb( ) ) != null ) {
                     int[] seekArgs = ( int[] )args;
                     FileTableEntry ftEnt = myTcb.getFtEnt( param );
                     if ( ftEnt != null )
                        return fs.seek( ftEnt, seekArgs[0], seekArgs[1] );
                  } 
                  return ERROR;
               case FORMAT:
                  return ( fs.format( param ) == true ) ? OK : ERROR;
               case DELETE:  
                  return ( fs.delete( (String)args ) == true ) ? OK : ERROR;
            }
            return ERROR;
         case INTERRUPT_DISK: 
            
            ioQueue.dequeueAndWakeup( COND_DISK_FIN );

          
            ioQueue.dequeueAndWakeup( COND_DISK_REQ );

            return OK;
         case INTERRUPT_IO:
            return OK;
      }
      return OK;
   }

 
   private static int sysExec( String args[] ) {
      String thrName = args[0]; 
      Object thrObj = null;

      try {
        
         Class thrClass = Class.forName( thrName ); 
         if ( args.length == 1 )
            thrObj = thrClass.newInstance( ); 
         else {
         
            String thrArgs[] = new String[ args.length - 1 ];
            for ( int i = 1; i < args.length; i++ )
               thrArgs[i - 1] = args[i];
            Object[] constructorArgs = new Object[] { thrArgs };

         
            Constructor thrConst 
               = thrClass.getConstructor( new Class[] {String[].class} );

           
            thrObj = thrConst.newInstance( constructorArgs );
         }
       
         Thread t = new Thread( (Runnable)thrObj );

         
         TCB newTcb = scheduler.addThread( t );
         return ( newTcb != null ) ? newTcb.getTid( ) : ERROR;
      }
      catch ( ClassNotFoundException e ) {
         System.out.println( e );
         return ERROR;
      }
      catch ( NoSuchMethodException e ) {
         System.out.println( e );
         return ERROR;
      }
      catch ( InstantiationException e ) {
         System.out.println( e );
         return ERROR;
      }
      catch ( IllegalAccessException e ) {
         System.out.println( e );
         return ERROR;
      }
      catch ( InvocationTargetException e ) {
         System.out.println( e );
         return ERROR;
      }
   }
}
