/*
Hamidou Diallo
Jeramie Castillo
*/
import java.util.Date;

public class Test3 extends Thread
{
    private int numThreadPairs;
    public static Object syncObj = new Object();

    public Test3()
    {
        numThreadPairs = 3;
    }

    public Test3(String[] args)
    {
        numThreadPairs = Integer.parseInt(args[0]);
    }

    public void run()  {
        long submissionTime = new Date().getTime();
        
        int tid;
        String name;
        for (int i = 0; i < numThreadPairs; i++)
        {
            SysLib.exec(SysLib.stringToArgs("TestThread3a"));
            SysLib.exec(SysLib.stringToArgs("TestThread3b"));
        }

        try
        {
            Thread.sleep(4000);
            synchronized (Test3.syncObj)
            {
                Test3.syncObj.notifyAll();
            }
        }
        catch(InterruptedException e) {}

        for (int i = 0; i < numThreadPairs * 2; i++)
        {
            SysLib.join();
        }

        long completionTime = new Date().getTime();

        SysLib.cout("Test3: =====================================" +
                "\n\tturnaround time = " + (completionTime - submissionTime) +
                "\n");

        SysLib.exit();
    }

}
