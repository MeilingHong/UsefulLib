package com.meiling.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/4 0004.
 */
public class FileDownloaderUtil {

    public static final String TEMP_DIR = "tempfiledir";
    private static String VERSION_APK = "base.apk";

    public static String getVersionApk() {
        return VERSION_APK;
    }

    public static void setVersionApk(String versionApk) {
        VERSION_APK = versionApk;
    }

    public static void updateAppVersion(Context context){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+
                File.separator+context.getPackageName()+ File.separator+TEMP_DIR+ File.separator
        +VERSION_APK))
                , "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void updateAppVersion(Context context,String fileName){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+
                        File.separator+context.getPackageName()+ File.separator+TEMP_DIR+ File.separator
                        +fileName))
                , "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
