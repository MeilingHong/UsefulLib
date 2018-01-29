package com.meiling.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/7/4 0004.
 */
public class FileDownloaderUtil {

    public static final String TEMP_DIR = "tempfiledir";
    private static String VERSION_APK = "base.apk";

    public static String getRootCacheDirectory(Context mContext) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                mContext.getApplicationContext().getPackageName() + File.separator + TEMP_DIR;
    }

    public static String getVersionApk() {
        return VERSION_APK;
    }

    public static void setVersionApk(String versionApk) {
        VERSION_APK = versionApk;
    }

    public static void updateAppVersion(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(getRootCacheDirectory(context) + File.separator
                        + VERSION_APK))
                , "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void updateAppVersion(Context context, String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(getRootCacheDirectory(context) + File.separator
                        + fileName))
                , "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
