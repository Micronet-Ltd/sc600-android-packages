
package com.swfp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Utils {
    private static final String TAG = "Utils";

	public Utils() {
        super();
    }

    public static float byte2float(byte[] buf, int index) {
        return Float.intBitsToFloat(((int)((((long)((((int)((((long)((((int)((((long)(buf[index] & 255)))
                 | (((long)buf[index + 1])) << 8))) & 65535))) | (((long)buf[index + 2])) << 16))) & 
                16777215))) | (((long)buf[index + 3])) << 24)));
    }

    public static int byteArray2Int(byte[] buf) {
        int v2 = 0;
        int v0;
        for(v0 = 0; v0 < 4; ++v0) {
            v2 += (buf[v0] & 255) << v0 * 8;
        }

        return v2;
    }

    public static int[] byteArray2IntArray(byte[] buf, int len) {
        if(len < 4) {
            throw new RuntimeException("byteArrayToIntArray: len is less than 4");
        }

        if(len % 4 != 0) {
            throw new RuntimeException("byteArrayToIntArray: len is not multiples of 4");
        }

        int[] v1 = new int[len / 4];
        int v3 = 0;
        int v0 = 0;

        while(v0 < len) {
            v3 += (buf[v0] & 255) << v0 * 8;
            if(v0 % 4 == 3) {
                v1[v0 / 4] = v3;
                v3 = 0;
            }

            ++v0;
        }

        return v1;
    }

    public static float[] byteArray2floatArray(byte[] buf, int len) {
        if(buf.length < len * 4) {
            throw new RuntimeException("function: byteArray2floatArray buf.length = " + buf.length + 
                    " but len = " + len);
        }

        float[] v4 = new float[len];
        int v2 = 0;
        int v1;
        for(v1 = 0; v1 < len * 4; ++v1) {
            int v0 = v1 / 4;
            switch(v1 % 4) {
                case 0: {
                    v2 = buf[v1] & 255;
                    break;
                }
                case 1: {
                    v2 = (((int)((((long)v2)) | (((long)buf[v1])) << 8))) & 65535;
                    break;
                }
                case 2: {
                    v2 = (((int)((((long)v2)) | (((long)buf[v1])) << 16))) & 16777215;
                    break;
                }
                case 3: {
                    v2 = ((int)((((long)v2)) | (((long)buf[v1])) << 24));
                    v4[v0] = Float.intBitsToFloat(v2);
                    break;
                }
            }
        }

        return v4;
    }

    public static byte[] kvalueToBmp(float[] fkv, int len) {
        byte[] v0 = new byte[len];
        int v1;
        for(v1 = 0; v1 < len; ++v1) {
            v0[v1] = ((byte)((((int)(fkv[v1] * 10000f))) & 255));
        }

        int v4 = v0[0];
        int v3 = v0[0];
        for(v1 = 0; v1 < len; ++v1) {
            if(v4 > v0[v1]) {
                v4 = v0[v1];
            }

            if(v3 < v0[v1]) {
                v3 = v0[v1];
            }
        }

        int v2 = v3 - v4 + 1;
        for(v1 = 0; v1 < len; ++v1) {
            v0[v1] = ((byte)((v0[v1] - v4) * 255 / v2));
        }

        return v0;
    }

    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        Bitmap v0 = null;
        if(origin == null) {
            return v0;
        }

        int v3 = origin.getWidth();
        int v4 = origin.getHeight();
        Matrix v5 = new Matrix();
        v5.setRotate(alpha);
        Bitmap v7 = Bitmap.createBitmap(origin, 0, 0, v3, v4, v5, false);
        if(v7.equals(origin)) {
            return v7;
        }

        origin.recycle();
        return v7;
    }

	/*
    public static void saveByteArrayToLocal(byte[] buf, int len, File file) {
        FileOutputStream v3;
        FileOutputStream v2 = null;
        try {
            v3 = new FileOutputStream(file);
            goto label_3;
        }
        catch(Throwable v4) {
        }
        catch(IOException v1) {
            goto label_13;
            try {
            label_3:
                v3.write(buf, 0, len);
                v3.flush();
                if(v3 == null) {
                    return;
                }

                goto label_6;
            }
            catch(Throwable v4) {
                v2 = v3;
            }
            catch(IOException v1) {
                v2 = v3;
                try {
                label_13:
                    v1.printStackTrace();
                    if(v2 == null) {
                        return;
                    }
                }
                catch(Throwable v4) {
                    goto label_29;
                }

                try {
                    v2.close();
                }
                catch(IOException v1) {
                    v1.printStackTrace();
                }

                return;
            }
            catch(FileNotFoundException v0) {
                v2 = v3;
                try {
                label_21:
                    v0.printStackTrace();
                    if(v2 == null) {
                        return;
                    }
                }
                catch(Throwable v4) {
                    goto label_29;
                }

                try {
                    v2.close();
                }
                catch(IOException v1) {
                    v1.printStackTrace();
                }

                return;
            }
        }
        catch(FileNotFoundException v0) {
            goto label_21;
        }

    label_29:
        if(v2 != null) {
            try {
                v2.close();
            }
            catch(IOException v1) {
                v1.printStackTrace();
            }
        }

        throw v4;
        try {
        label_6:
            v3.close();
        }
        catch(IOException v1) {
            v1.printStackTrace();
        }
    }
	*/

    public static byte[] translateImageCode(byte[] imput, int col, int row) {
        byte[] v6 = new byte[]{66, 77, 0, 0, 0, 0, 0, 0, 0, 0, 54, 4, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 
                0, 0, 0, 0, 1, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
                0, 0, 0, 0};
        byte[] v0 = new byte[1078];
        byte[] v3 = new byte[col * row + 1078];
        System.arraycopy(v6, 0, v0, 0, v6.length);
        long v4 = ((long)col);
        v0[18] = ((byte)(((int)(255 & v4))));
        v4 >>= 8;
        v0[19] = ((byte)(((int)(255 & v4))));
        v4 >>= 8;
        v0[20] = ((byte)(((int)(255 & v4))));
        v0[21] = ((byte)(((int)(255 & v4 >> 8))));
        v4 = ((long)row);
        v0[22] = ((byte)(((int)(255 & v4))));
        v4 >>= 8;
        v0[23] = ((byte)(((int)(255 & v4))));
        v4 >>= 8;
        v0[24] = ((byte)(((int)(255 & v4))));
        v0[25] = ((byte)(((int)(255 & v4 >> 8))));
        int v2 = 0;
        int v1;
        for(v1 = 54; v1 < 1078; v1 += 4) {
            byte v7 = ((byte)v2);
            v0[v1 + 2] = v7;
            v0[v1 + 1] = v7;
            v0[v1] = v7;
            v0[v1 + 3] = 0;
            ++v2;
        }

        System.arraycopy(v0, 0, v3, 0, v0.length);
        System.arraycopy(imput, 0, v3, 1078, col * row);
        return v3;
    }
    
    
    
    public static String getRamTotalSize(Context context){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }
    
    
    /** 
    * 获得SD卡总大小 
    * 
    * @return 
    */
    public String getSDTotalSize(Context context) { 
      File path = Environment.getExternalStorageDirectory(); 
      StatFs stat = new StatFs(path.getPath()); 
      long blockSize = stat.getBlockSize(); 
      long totalBlocks = stat.getBlockCount(); 
      return Formatter.formatFileSize(context, blockSize * totalBlocks); 
    } 
    /** 
    * 获得sd卡剩余容量，即可用大小 
    * 
    * @return 
    */
    public String getSDAvailableSize(Context context) { 
      File path = Environment.getExternalStorageDirectory(); 
      StatFs stat = new StatFs(path.getPath()); 
      long blockSize = stat.getBlockSize(); 
      long availableBlocks = stat.getAvailableBlocks(); 
      return Formatter.formatFileSize(context, blockSize * availableBlocks); 
    } 
    /** 
    * 获得机身存储内存总大小 
    * 
    * @return 
    */
    public static  String getRomTotalSize(Context context) { 
      /*File path = Environment.getDataDirectory(); 
      StatFs stat = new StatFs(path.getPath()); 
      long blockSize = stat.getBlockSize(); 
      long totalBlocks = stat.getBlockCount(); */
      return getPrivateStorageInfo(context);
   //   return getRomFromEmmc(context);//Formatter.formatFileSize(context, blockSize * totalBlocks); 
    } 
    
    public static final String STORAGE_INFO = "/sys/block/mmcblk0/size";
    private static long roundStorageSize(long size) {
        long val = 1;
        long pow = 1;
        while ((val * pow) < size) {
            val <<= 1;
            if (val > 512) {
                val = 1;
                pow *= 1000;
            }
        }
        return val * pow;
    }
    private static String getRomFromEmmc(Context context){
    	  String storage_info= "";
          long storageValue = 0;
          try {
              storage_info = readFile(STORAGE_INFO);
             String storage_info1 = storage_info.substring(0);
              storageValue = Long.valueOf(storage_info1);
              storageValue = roundStorageSize(storageValue * 512);
          } catch (Exception e) {
              // TODO: handle exception
          }

          return Formatter.formatFileSize(context, storageValue);
    }
    
    private static String readFile(String filePath) {
        String res = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {
            String str = null;
            while ((str = br.readLine()) != null) {
                res += str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    
    
    public static String getPrivateStorageInfo(Context mContext) {
    	StorageManager sm = mContext.getSystemService(StorageManager.class);
        long totalInternalStorage = sm.getPrimaryStorageSize();
        long privateFreeBytes = 0;
        long privateTotalBytes = 0;
        for (VolumeInfo info : sm.getVolumes()) {
            final File path = info.getPath();
            if (info.getType() != VolumeInfo.TYPE_PRIVATE || path == null) {
                continue;
            }
            privateTotalBytes += getTotalSize(info, totalInternalStorage);
            privateFreeBytes += path.getFreeSpace();
        }
        return   Formatter.formatFileSize(mContext, privateTotalBytes);
        
    }
    
    public static long getTotalSize(VolumeInfo info, long totalInternalStorage) {
        // Device could have more than one primary storage, which could be located in the
        // internal flash (UUID_PRIVATE_INTERNAL) or in an external disk.
        // If it's internal, try to get its total size from StorageManager first
        // (totalInternalStorage), because that size is more precise because it accounts for
        // the system partition.
        if (info.getType() == VolumeInfo.TYPE_PRIVATE
                && Objects.equals(info.getFsUuid(), StorageManager.UUID_PRIVATE_INTERNAL)
                && totalInternalStorage > 0) {
            return totalInternalStorage;
        } else {
            final File path = info.getPath();
            if (path == null) {
                // Should not happen, caller should have checked.
                Log.e(TAG, "info's path is null on getTotalSize(): " + info);
                return 0;
            }
            return path.getTotalSpace();
        }
    }
    
    /** 
    * 获得机身可用存储内存 
    * 
    * @return 
    */
    public static String getRomAvailableSize(Context context) { 
      File path = Environment.getDataDirectory(); 
      StatFs stat = new StatFs(path.getPath()); 
      long blockSize = stat.getBlockSize(); 
      long availableBlocks = stat.getAvailableBlocks(); 
      return Formatter.formatFileSize(context, blockSize * availableBlocks); 
    }
}

