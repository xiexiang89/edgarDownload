package com.edgar.download.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * Created by Edgar on 2016/7/5.
 */
public final class StorageUtils {
    private static final String SD_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final StatFs STAT_FS = new StatFs(SD_ROOT_PATH);

    private StorageUtils(){}

    public static long getTotalSize(){
        long blockSize = 0;
        long blockCount = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            blockSize =  STAT_FS.getBlockSizeLong();
            blockCount = STAT_FS.getBlockCountLong();
        } else {
            blockSize = STAT_FS.getBlockSize();
            blockCount = STAT_FS.getBlockCount();
        }
        return blockSize * blockCount;
    }

    public static long getAvailableSize(){
        StatFs statFs = new StatFs(SD_ROOT_PATH);
        long available = 0;
        long totalSize = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            available = statFs.getAvailableBlocksLong();
            totalSize = statFs.getBlockSizeLong();
        } else {
            available = (long) statFs.getAvailableBlocks();
            totalSize = (long) statFs.getBlockSize();
        }
        return available * totalSize;
    }

    public static boolean isExternalStorageRemovable(){
        return Environment.isExternalStorageRemovable();
    }

    public static boolean isExternalStorageAvailable(){
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    public static boolean isAvailable(long size){
        return getAvailableSize() > size;
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getUsableSpace(File path) {
        if (Utils.hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }
}
