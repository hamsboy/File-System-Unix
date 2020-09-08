/*
Hamidou Diallo
Jeramie Castillo
*/
import java.util.*;
public class Directory
{
    // max characters of each file name
    private static int MAX_CHARS = 30;
    private static int BLOCK_SIZE = 4;
    private static short ERROR = -1;

    
    private int fsizes[];
    private char fnames[][];

    public Directory(int maxInumber)
    {
        
        if (maxInumber <= 0) {
        
            SysLib.cout("The number of files should be > 0\n");
            return;
        }
        fsizes = new int[maxInumber];     
        for (int i = 0; i < maxInumber; i++)
        {
            fsizes[i] = 0;                 
        }
        fnames = new char[maxInumber][MAX_CHARS];
        String root = "/";                  
        fsizes[0] = root.length();         
        root.getChars(0, fsizes[0], fnames[0], 0); 
    }


    
    public void bytes2directory(byte data[])
    {
        int offset = 0;
        for(int position = 0; position < fsizes.length; position++)
        {
            fsizes[position] = SysLib.bytes2int(data, offset);
            offset += BLOCK_SIZE;
        }
        for(int position = 0; position < fnames.length; position++)
        {
            String fname = new String(data, offset, (MAX_CHARS) * 2);
            fname.getChars(0, fsizes[position], fnames[position], 0);
            offset += (MAX_CHARS * 2);
        }
    }

    
    public byte[] directory2bytes()
    {
        int offset = 0;
        byte[] data = new byte[(fsizes.length * 4) + fnames.length * MAX_CHARS * 2];
        for(int position = 0; position < fsizes.length; position++)
        {
            SysLib.int2bytes(fsizes[position], data, offset);
            offset += BLOCK_SIZE;
        }
        for(int position = 0; position < fnames.length; position++)
        {
            String newString = new String(fnames[position], 0, fsizes[position]);
            byte[] tempData = newString.getBytes();
            System.arraycopy(tempData, 0, data, offset, tempData.length);
            offset += (MAX_CHARS * 2);
        }
        return data;
    }

   

    public short ialloc(String filename)
    {
        for(short position = 0; position < fsizes.length; position++)
        {
            if(fsizes[position] == 0)
            {
                fsizes[position] = Math.min(MAX_CHARS, filename.length());
                filename.getChars(0, fsizes[position], fnames[position], 0);
                return position;
            }
        }
        return ERROR;
    }

   
    public boolean ifree(short iNumber)
    {
        if(fsizes[iNumber] > 0)
        {
            fsizes[iNumber] = 0;
            return true;
        }
        return false;
    }

    public short namei(String filename)
    {

        for(short position = 0; position < fsizes.length; position++)
        {
            if(filename.length() == fsizes[position])
            {
                String tempString = new String(fnames[position], 0, fsizes[position]);
                if(filename.equals(tempString))
                {
                    return position;
                }
            }
        }
        return ERROR;
    }
}
