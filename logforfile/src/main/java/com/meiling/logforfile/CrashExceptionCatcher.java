package com.meiling.logforfile;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 16:10.
 *
 * @package com.meiling.logforfile
 * @auther By MeilingHong
 * @emall marisareimu123@gmail.com
 * @date 2018-02-01   16:10
 */

public class CrashExceptionCatcher implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    private boolean isUploadingLogFile = false;
    private IUploadLogFileToServer uploadLogFileToServer;
    private ExecutorService mSingleService;

    private CrashExceptionCatcher() {
        mSingleService = Executors.newSingleThreadExecutor();
    }

    public static CrashExceptionCatcher getInstances() {
        return CrashExceptionCatcherHolder.instances;
    }

    /**
     * 建议在子线程中完成初始化操作，避免由于IO操作导致的UI卡顿
     *
     * @param ctx
     */
    public void init(final Context ctx) {
        synchronized (CrashExceptionCatcherHolder.instances) {
            //避免用户忘记在线程中进行操作，增加线程池来保障UI的流畅
            mSingleService.submit(new Runnable() {
                @Override
                public void run() {
                    mContext = ctx.getApplicationContext();
                    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
                    //TODO 保证用户设置为上传文件的情况下，上传文件，
                    if (isUploadingLogFile && uploadLogFileToServer != null) {
                        File directory = new File(SaveLogUtil.getInstances().getRootLogDirPath(mContext));
                        if (directory != null && directory.isDirectory() &&
                                directory.listFiles() != null && directory.listFiles().length > 0) {
                            boolean isClearLogFiles = uploadLogFileToServer.uploadLogFiles(directory.listFiles());
                            if (isClearLogFiles) {
                                //TODO 清除问价夹下的所有文件
                                SaveLogUtil.getInstances().deleteAllFileInDir(directory);
                            }
                        }
                    }
                    Thread.setDefaultUncaughtExceptionHandler(CrashExceptionCatcher.this);
                }
            });
        }
    }

    public CrashExceptionCatcher setUploadSwitch(boolean isUploadLogFile, IUploadLogFileToServer iUploadLogFileToServer) {
        isUploadingLogFile = isUploadLogFile;
        uploadLogFileToServer = iUploadLogFileToServer;
        return CrashExceptionCatcherHolder.instances;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        }
        //TODO 记录下所有未捕捉的异常信息（）
        collectCrashInfomation(e);
    }

    private void collectCrashInfomation(Throwable ex) {
        //
        if (ex == null) {//不处理空异常
            return;
        }
        final String packageName = getPackageInfo();
        final String deviceInformation = generateDeviceInfo();
        final String msg = ex.getLocalizedMessage();// 生成错误信息
        final String msgDetail = getExceptionDetailInformation(ex);// 生成错误信息
        SaveLogUtil.getInstances().addIntoLogFile(mContext, packageName + deviceInformation + msg + msgDetail);
    }

    public String getExceptionDetailInformation(Throwable ex) {
        if (ex == null) {//不处理空异常
            return "";
        }
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        // printStackTrace(PrintWriter s)
        // 将此 throwable 及其追踪输出到指定的 PrintWriter
        ex.printStackTrace(printWriter);

        // getCause() 返回此 throwable 的 cause；如果 cause 不存在或未知，则返回 null。
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        // toString() 以字符串的形式返回该缓冲区的当前值。
        String result = info.toString();
        printWriter.close();
        return result;
    }

    public String getPackageInfo() {
        try {
            StringBuilder builder = new StringBuilder();
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            builder.append("PackageName:" + mContext.getPackageName() + "\n");
            if (pi != null) {
                builder.append("VersionName:" + pi.versionName + "\n");
                builder.append("VersionCode:" + pi.versionCode + "\n");
            }
            return builder.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 生成设备信息，并转换成String
     *
     * @return
     */
    private String generateDeviceInfo() {
        StringBuilder builder = new StringBuilder();

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                // setAccessible(boolean flag)
                // 将此对象的 accessible 标志设置为指示的布尔值。
                // 通过设置Accessible属性为true,才能对私有变量进行访问，不然会得到一个IllegalAccessException的异常
                field.setAccessible(true);
//                mDeviceCrashInfo.put(field.getName(), String.valueOf(field.get(null)));
                LogUitl.e(field.getName() + " : " + String.valueOf(field.get(null)));
                builder.append(field.getName() + " : " + String.valueOf(field.get(null)) + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    private static class CrashExceptionCatcherHolder {
        private static CrashExceptionCatcher instances = new CrashExceptionCatcher();
    }
}
