/*
Hamidou Diallo
Jeramie Castillo
*/
import java.util.Vector;

public class FileTable
{
  private final static int UNUSED = 0;     
  private final static int USED = 1;
  private final static int READ = 2;
  private final static int WRITE = 3;
  private final static int DELETE = 4;

  private Vector <FileTableEntry> table;    
  private Directory dir;                    

 
  public FileTable(Directory directory)
  {
     table = new Vector<FileTableEntry>();   
     dir = directory;                        
  }

  
  public synchronized FileTableEntry falloc(String filename, String mode)
  {
     short iNumber = -1;
      Inode inode = null;
      while(true)
      {
          iNumber = (filename.equals("/") ? 0 : dir.namei(filename));

          if(iNumber >= 0)
          {
              inode = new Inode(iNumber);
              if(mode.compareTo("r") == 0)
              {
                  if((inode.flag == UNUSED) || (inode.flag == USED) )
                  {
                      inode.flag = USED;
                      break;
                  }
                  try
                  {
                      wait();
                  }
                  catch (InterruptedException e) {}
              }
              else
              {

                  if ((inode.flag == UNUSED) || (inode.flag == WRITE))
                  {
                      inode.flag = READ;
                      break;
                  }
                  if ((inode.flag == USED) || (inode.flag == READ))
                  {
                      inode.flag = WRITE;
                      break;
                  }
                  try
                  {
                      wait();
                  }
                  catch (InterruptedException e) {}
              }
          }
          else if (!mode.equals("r"))
          {
              iNumber = dir.ialloc(filename);
              inode = new Inode(iNumber);
              inode.flag = READ;
              break;
          }
          else
          {
              return null;
          }
      }
      inode.count++;
      inode.toDisk((short)iNumber);
      FileTableEntry newEntry = new FileTableEntry(inode, iNumber, mode);
      table.addElement(newEntry);
      return newEntry;
  }

 

  public synchronized boolean ffree(FileTableEntry ftEntry)
  {


    if (table.removeElement(ftEntry))
    {
        Inode inode = ftEntry.inode;
        if(inode.flag == READ || inode.flag == WRITE)
        {
            inode.flag = USED;
        } else {
            inode.flag = UNUSED;
        }
        inode.count--;
        inode.toDisk(ftEntry.iNumber);
        notify();
        return true;
    }
    return false;
  }

  public synchronized boolean fempty()
  {
     return table.isEmpty();
  }
}
