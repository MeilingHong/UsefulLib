package com.meiling.download;

import android.content.Context;

import java.io.File;

/**
 * Created by Administrator on 2018/1/29.
 */

public class DownloadFileClearUtil {
    public static int clearAllCache(Context activity) {
        File root = new File(FileDownloaderUtil.getRootCacheDirectory(activity));
        if (root == null) {
            return IDownloadFileClearMsg.DIR_EMPTY;
        } else if (root != null && root.isDirectory()) {
            deleteAllFileInDir(root);
            return IDownloadFileClearMsg.DELETE_DIRECTORY;
        } else if (root != null && root.isFile()) {
            root.delete();
            return IDownloadFileClearMsg.DELETE_FILE;
        } else {
            return IDownloadFileClearMsg.UNKNOW_ERROR;
        }
    }

    // delete all file in specific directory
    public static void deleteAllFileInDir(File thisDir) {
        if (thisDir == null) {
            return;
        } else {
            if (thisDir.isFile()) {
                thisDir.delete();// 避免由于正在访问文件而导致的删除出现异常
            } else {// file is directory
                File[] temp = thisDir.listFiles();
                for (File subFile :
                        temp) {
                    if (subFile != null) {
                        if (subFile.isFile()) {
                            subFile.delete();
                        } else {
                            deleteAllFileInDir(subFile);
                        }
                    }
                }
            }
        }
    }
}
