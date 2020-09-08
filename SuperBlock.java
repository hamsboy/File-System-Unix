/*
Hamidou Diallo
Ahmed Osman
*/
public class SuperBlock {
  private final int defaultInodeBlocks = 64;
  public int totalBlocks;
  public int totalInodes;
  public int freeList;
 // public short freeList;

  public SuperBlock(int diskSize) {
   
	byte[] superBlock = new byte[Disk.blockSize];

	SysLib.rawread(0, superBlock);
	totalBlocks = SysLib.bytes2int(superBlock, 0);
  SysLib.cout("totalBlocks in SuperBlock() now " + totalBlocks + "\n");
	totalInodes = SysLib.bytes2int(superBlock, 4);
  SysLib.cout("totalInodes in SuperBlock() now " + totalInodes + "\n");
	freeList = SysLib.bytes2int(superBlock, 8);
    
	if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {

	    return;
	} else {
      
      totalBlocks = diskSize;
	  SysLib.cout("default format ( " + defaultInodeBlocks + " )\n");
      format(defaultInodeBlocks);
    }
  }


  public synchronized void format(int files){
  
    byte[] superBlock = new byte[512];
    totalBlocks = 1000;
    totalInodes = files;
	  freeList = (files % 16) == 0 ? files / 16 + 1 : files / 16 + 2;
  
    SysLib.int2bytes(totalBlocks, superBlock, 0);
    SysLib.int2bytes(totalInodes, superBlock, 4);
    SysLib.int2bytes(freeList, superBlock, 8);


    SysLib.cout("totalBlocks in SuperBlock() now " + totalBlocks + "\n");
    SysLib.cout("totalInodes in SuperBlock() now " + totalInodes + "\n");



	  SysLib.cout("in superblock format");  
    SysLib.rawwrite(0, superBlock);

	  byte[] data = new byte[512];
	 
	  for (short i = (short)freeList; i < totalBlocks; i++) {
     
      for (int j = 0; j < Disk.blockSize; j++) {
        data[j] = (byte)0;
      }

      short next_block = (short)((i == totalBlocks - 1) ? 0 : i + 1);
      SysLib.cout("next free block is " + next_block + "\n");
      SysLib.short2bytes(next_block, data, 0);


     
		  SysLib.rawwrite(i, data); 
    }
  }

 
  public void sync()
  {
   
    int offset = 0;
    byte[] superBlock = new byte[Disk.blockSize];
    SysLib.rawread(0, superBlock);
    SysLib.int2bytes(totalBlocks, superBlock, 0);
    SysLib.int2bytes(totalInodes, superBlock, 4);
    SysLib.int2bytes(freeList, superBlock, 8);
    SysLib.rawwrite(0, superBlock);
  }


 public short getFreeBlock() {
    
    short ret = (short)freeList;
    
    byte[] data = new byte[Disk.blockSize];
    SysLib.rawread(freeList, data);
    
    freeList = (int)SysLib.bytes2short(data, 0);
    SysLib.cout("SUPERBLOCK~~~~~~~WAS " + ret + " is now " + freeList + "\n");

    return ret;
  }

 
  public void returnBlock(short blockNumber) {

    short last_free = (short)freeList;
    short next_free = 0;
    byte[] current_end = null;  
    byte[] new_end = null;  
    
   
    SysLib.rawread(blockNumber, new_end);
    SysLib.short2bytes((short)0, new_end, 0);
    SysLib.rawwrite(blockNumber, new_end);
    
    while (last_free < totalBlocks) { 
      SysLib.rawread(last_free, current_end);
   
      next_free = SysLib.bytes2short(current_end, 0);
      if (next_free == 0) {
       
        SysLib.short2bytes(blockNumber, current_end, 0);
        SysLib.rawwrite(last_free, current_end);

        
        return;
      }
     
      last_free = next_free;
    }
  }
}