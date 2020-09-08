/*
Hamidou Diallo
Jeramie Castillo
*/
public class Inode
{
    private final static int iNodeSize = 32;       // fix to 32 bytes
    private final static int directSize = 11;      // # direct pointers

    public int length;                             // file size in bytes
    public short count;                            // # file-table entries pointing to this
    public short flag;                             // 0 = unused, 1 = used, ...
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                         // a indirect pointer

    /*
    Default constructor initializes empty Inode.
     */
    public Inode()
    {
       length = 0;
       count = 0;
       flag = 1;
       for (int i = 0; i < directSize; i++)
       {
          direct[i] = -1;
       }
       indirect = -1;
    }

    /*
     Constructor to create an Inode given an iNumber.
     */
    public Inode(short iNumber)
    {
       
 	   int blockNumber = findTargetBlock(iNumber);
 	   byte[] data = new byte[Disk.blockSize];
 	   SysLib.rawread(blockNumber, data); 
 	   int offset = ((iNumber % 16) * iNodeSize);

 	   length = SysLib.bytes2int(data, offset);
 	   count = SysLib.bytes2short(data, offset + 4);
 	   flag = SysLib.bytes2short(data, offset + 6);

 	   for (int i = 0; i < directSize; i++) {
 	       direct[i] = SysLib.bytes2short(data, (offset + 8 + 2 * i));
       }
       indirect = SysLib.bytes2short(data, offset + 30);
    }


   
    public int toDisk(short iNumber)
    {
      
        int blockNumber = findTargetBlock(iNumber);
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(blockNumber, data);
        int offset = ((iNumber % 16) * iNodeSize);

       SysLib.int2bytes(length, data, offset);
       SysLib.short2bytes(count, data, offset+ 4);
       SysLib.short2bytes(flag, data, offset + 6);

       for (int i = 0; i < directSize; i++) {
             SysLib.short2bytes(direct[i], data, (offset+ 8 + 2 * i));
        }
        SysLib.short2bytes(indirect, data, offset + 30);

       int writeResult = SysLib.rawwrite(blockNumber, data);
       if (writeResult != Kernel.OK ) {
           return -1;
       }
        return 0;
    }

   
    public short findTargetBlock(int iNumber)
    {
        if (iNumber >= 0 ) {
            return (short) (iNumber / 16 + 1);
        }

        return -1;
    }

   
    public short seekDirectBlock (int seekPtr)
    {
        if (seekPtr < direct.length * Disk.blockSize) {
            return (short) (seekPtr / Disk.blockSize);
        }
        return -1;
    }

}
