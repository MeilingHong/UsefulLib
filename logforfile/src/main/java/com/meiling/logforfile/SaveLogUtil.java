package com.meiling.logforfile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 将Log日志输出独立出来，作为公共可引用的部分
 *
 * @package com.meiling.logforfile
 * @auther By MeilingHong
 * @emall marisareimu123@gmail.com
 * @date 2018-01-29   16:32
 */
public class SaveLogUtil {
    //
    private static final String RECORD_FILE_SP = "spForErrorLog";
    private static final String RECORD_FILE_KEY = "CurrentErrorLogFileName";

    private static final String ROOT_DIR_NAME = "errorLogDir";
    private static final String COMMON_ROOT_DIR_NAME = "errorLogRecordDir";
    private static final String COMMON_ROOT_DIR_RECORD_FILE_PATH = "errorLogRecordFilePath.txt";

    private static final String PREFIX_HEAD_LOGFILE = "ERROR_LOG_";
    private static final String PREFIX_OF_LOGFILE = "yyyyMMdd_HHmmss";
    private static final String POSTFIX_OF_LOGFILE = ".txt";

    private static final int LIMIT_LOG_FILE_SIZE = 2 * 1024 * 1024;//2M

    private static final String LOG_HEAD = "yyyy-MM-dd HH:mm:ss";

    private SimpleDateFormat timeFormat;
    private SimpleDateFormat logFormat;
    private String currentLogFileName;

    private final Queue<String> logMsgQueue;
    private final ExecutorService logSavingPool;


    private SaveLogUtil() {
        logMsgQueue = new LinkedBlockingDeque<>();
        logSavingPool = Executors.newSingleThreadExecutor();
        timeFormat = new SimpleDateFormat(PREFIX_OF_LOGFILE);
        logFormat = new SimpleDateFormat(LOG_HEAD);
    }

    public static SaveLogUtil getInstances() {
        return LOGHolder.instance;
    }

    /**
     * 公共的日志保存类-----需要指定保存至的文件路径
     *
     * @param filePath
     */
    protected void saveLogIntoFile(String filePath) {
        //日志信息追加至指定文件路径的末尾
        try {
            String tempMsg = null;
            File recordFile = new File(filePath);
            BufferedWriter bufferedWriter = null;
            //使用UTF-8编码写入文件
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordFile, true), "UTF-8"), 1024);
//            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordFile, true),"GBK"), 1024);
            while (!logMsgQueue.isEmpty()) {
                tempMsg = logFormat.format(new Date()) + logMsgQueue.poll();
                if (tempMsg != null) {
                    bufferedWriter.write(tempMsg + "\r\n");
                    /**
                     newLine 方法会调用系统的换行符。而这就是问题的根本。
                     不同系统的换行符：
                     windows -->   \r\n
                     Linux         -->   \r
                     mac         -->   \n
                     */
                    bufferedWriter.flush();
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //************************************************************************************
    // 应用相关，日志路径记录

    /**
     * 获取保存的错误日志文件名
     *
     * @param context
     * @return
     */
    private String getCurrentErrorLogFileName(Context context) {
        return context.getSharedPreferences(RECORD_FILE_SP, Context.MODE_PRIVATE).getString(RECORD_FILE_KEY, null);
    }

    /**
     * 设置缓存的当前错误日志文件名
     *
     * @param context
     * @param currentFileName
     */
    private void setCurrentErrorLogFileName(Context context, String currentFileName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(RECORD_FILE_SP, Context.MODE_PRIVATE).edit();
        editor.putString(RECORD_FILE_KEY, currentFileName);
        editor.commit();
    }

    /**
     * 创建错误日志目录
     *
     * @param context
     */
    private void createNewLogFileDir(Context context) {
        File dir = new File(getRootLogDirPath(context));
        if (dir.exists() && dir.isDirectory()) {
            return;
        } else if (dir.exists()) {
            dir.delete();
            dir.mkdirs();
        } else {
            dir.mkdirs();
        }
    }


    /**
     * 执行创建日志文件的步骤
     *
     * @param context
     * @return
     */
    private boolean createNewLogFile(Context context) {
        try {
            createNewLogFileDir(context);//保证目录创建出来
            File logFile = new File(createNewLogFileName(context));
            if (logFile.exists()) {
                for (int i = 1; i < 1000000; i++) {
                    logFile = new File(createNewLogFileName(context, i));
                    if (logFile.exists()) {
                        continue;
                    } else {
                        logFile.createNewFile();
                        break;
                    }
                }
                currentLogFileName = logFile.getAbsolutePath();
                setCurrentErrorLogFileName(context, currentLogFileName);
                return true;
            } else {
                //创建文件，并缓存这个文件名
                logFile.createNewFile();
                currentLogFileName = logFile.getAbsolutePath();
                setCurrentErrorLogFileName(context, currentLogFileName);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 新错误日志文件路径
     *
     * @param context
     * @return
     */
    private String createNewLogFileName(Context context) {
        return getRootLogDirPath(context) + File.separator + PREFIX_HEAD_LOGFILE + timeFormat.format(new Date()) + POSTFIX_OF_LOGFILE;
    }

    /**
     * 新错误日志文件路径(应对短时间内产生了大量日志的情况)
     *
     * @param context
     * @param count
     * @return
     */
    private String createNewLogFileName(Context context, int count) {
        return getRootLogDirPath(context) + File.separator + PREFIX_HEAD_LOGFILE + timeFormat.format(new Date()) + "_" + count + POSTFIX_OF_LOGFILE;
    }

    /**
     * 错误日志根目录路径
     *
     * @param context
     * @return
     */
    private String getRootLogDirPath(Context context) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                context.getApplicationContext().getPackageName() + File.separator + ROOT_DIR_NAME;
    }

    /**
     * 应用相关的的日志保存方法
     *
     * @param context
     * @param logMsg
     */
    public void addIntoLogFile(final Context context, final String logMsg) {
        if (StorageUtil.externalMemoryAvailable()// 保证外存挂载
                && IStorageInfo.SIZE_512M < StorageUtil.getAvailableExternalMemorySize()
                ) {//保证外存存在至少512M以上的空间，才允许进行日志记录（尽可能保证日志记录，不影响系统的运行）
            synchronized (logMsgQueue) {//
                logMsgQueue.add(logMsg);
                logSavingPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        //可以进行外存读写
                        //1、判断是否存在上一个记录文件，如果丢失，则创建一个新的文件进行记录
                        currentLogFileName = getCurrentErrorLogFileName(context);
                        if (currentLogFileName == null) {
                            //直接创建新的日志文件--并记录
                            createNewLogFile(context);
                            saveLogIntoFile(currentLogFileName);
                        } else {
                            // 检查这个文件是否超出限定大小
                            File logFile = new File(currentLogFileName);
                            if (logFile.length() > LIMIT_LOG_FILE_SIZE) {
                                // 超出限定大小--------重新创建一个日志文件
                                createNewLogFile(context);
                                saveLogIntoFile(currentLogFileName);
                            } else {
                                //未超出大小限制，则在文件基础上进行追加
                                saveLogIntoFile(currentLogFileName);
                            }
                        }
                    }
                });
            }
        } else if (StorageUtil.externalMemoryAvailable() && IStorageInfo.SIZE_512M >= StorageUtil.getAvailableExternalMemorySize()) {
            throw new RuntimeException("Storage strategy applied,saving log info is forbidden when storage space less than 512M!");
        } else if (!StorageUtil.externalMemoryAvailable()) {
            throw new RuntimeException("External storage no available for saving log!");
        } else {
            throw new RuntimeException("Unknown error for saving log!");
        }
    }

    //************************************************************************************
    // 非应用相关的错误日志文件的文件名保存于获取

    /**
     * 获取保存的错误日志文件名（非特定应用），使用文件进行保存
     * <p>
     * 1、若存在保存文件名字的文件，则从文件中读取该文件路径（该文件中仅保存一个数据）
     *
     * @return
     */
    private String getCurrentErrorLogFileName() {
        return loadLogFilePathFromFile();
    }

    /**
     * 设置缓存的当前错误日志文件名
     *
     * @param currentFileName 当前用于保存日志的文件的路径
     */
    private void setCurrentErrorLogFileName(String currentFileName) {
        //先创建目录--保证文件目录存在
        createNewLogFileDir();
        //创建记录文件目录
        createRecordPathFile();
        saveLogFilePathIntoFile(currentFileName);
    }

    private void saveLogFilePathIntoFile(String logFilePath) {
        try {
            File recordFile = new File(getRecordFilePath());
            if (!recordFile.exists()) {
                throw new RuntimeException("Log file path record file not Found!");
            }
            BufferedWriter bufferedWriter = null;
            //使用UTF-8编码写入文件
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordFile, true), Charset.forName("UTF-8")), 1024);
//            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordFile, true),"GBK"), 1024);
            bufferedWriter.write(logFilePath);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadLogFilePathFromFile() {
        try {
            File recordFile = new File(getRecordFilePath());
            if (!recordFile.exists()) {
//            throw new RuntimeException("Log file path record file not Found!");
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(recordFile), Charset.forName("UTF-8")), 1024);
            String currentLogFilePath = reader.readLine();
            reader.close();//释放IO流避免过多占用资源导致Crash
            return currentLogFilePath;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createRecordPathFile() {
        //创建记录文件的目录
        createNewLogRecordFileDir();
        //创建对应的文件
        try {
            File recordFile = new File(getRecordFilePath());
            if (!recordFile.exists()) {//文件不存在，则创建该文件
                recordFile.createNewFile();
            } else {
                if (recordFile.isDirectory()) {
                    recordFile.delete();
                    recordFile.createNewFile();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRecordFilePath() {
        return getRootLogRecordDirPath() + File.separator + COMMON_ROOT_DIR_RECORD_FILE_PATH;
    }

    /**
     * 创建错误日志目录
     */
    private void createNewLogFileDir() {
        File dir = new File(getRootLogDirPath());
        if (dir.exists() && dir.isDirectory()) {
            return;
        } else if (dir.exists()) {
            dir.delete();
            dir.mkdirs();
        } else {
            dir.mkdirs();
        }
    }

    /**
     * 错误日志根目录路径 （根目录下创建一个文件夹，由于这样跟应用无关了，所以如果需要区分的话，只能在日志文件中，通过日志文件头中的信息进行区分）
     *
     * @return
     */
    private String getRootLogDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + ROOT_DIR_NAME;
    }

    /**
     * 创建错误日志目录
     */
    private void createNewLogRecordFileDir() {
        File dir = new File(getRootLogRecordDirPath());
        if (dir.exists() && dir.isDirectory()) {
            return;
        } else if (dir.exists()) {
            dir.delete();
            dir.mkdirs();
        } else {
            dir.mkdirs();
        }
    }

    /**
     * 错误日志根目录路径 （根目录下创建一个文件夹，由于这样跟应用无关了，所以如果需要区分的话，只能在日志文件中，通过日志文件头中的信息进行区分）
     *
     * @return
     */
    private String getRootLogRecordDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + COMMON_ROOT_DIR_NAME;
    }

    /**
     * 新错误日志文件路径
     *
     * @return
     */
    private String createNewLogFileName() {
        return getRootLogDirPath() + File.separator + PREFIX_HEAD_LOGFILE + timeFormat.format(new Date()) + POSTFIX_OF_LOGFILE;
    }

    /**
     * 新错误日志文件路径(应对短时间内产生了大量日志的情况)
     *
     * @param count
     * @return
     */
    private String createNewLogFileName(int count) {
        return getRootLogDirPath() + File.separator + PREFIX_HEAD_LOGFILE + timeFormat.format(new Date()) + "_" + count + POSTFIX_OF_LOGFILE;
    }

    /**
     * 执行创建日志文件的步骤  ( 非应用关联的日志文件)
     *
     * @return
     */
    private boolean createNewLogFile() {
        try {
            createNewLogFileDir();//保证目录创建出来
            File logFile = new File(createNewLogFileName());
            if (logFile.exists()) {
                for (int i = 1; i < 1000000; i++) {
                    logFile = new File(createNewLogFileName(i));
                    if (logFile.exists()) {
                        continue;
                    } else {
                        logFile.createNewFile();
                        break;
                    }
                }
                currentLogFileName = logFile.getAbsolutePath();
                setCurrentErrorLogFileName(currentLogFileName);
                return true;
            } else {
                //创建文件，并缓存这个文件名
                logFile.createNewFile();
                currentLogFileName = logFile.getAbsolutePath();
                setCurrentErrorLogFileName(currentLogFileName);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 应用无关的日志保存方法
     *
     * @param logMsg
     */
    public void addIntoLogFile(final String logMsg) {
        if (StorageUtil.externalMemoryAvailable()// 保证外存挂载
                && IStorageInfo.SIZE_512M < StorageUtil.getAvailableExternalMemorySize()
                ) {//保证外存存在至少512M以上的空间，才允许进行日志记录（尽可能保证日志记录，不影响系统的运行）
            synchronized (logMsgQueue) {//
                logMsgQueue.add(logMsg);
                logSavingPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        //可以进行外存读写
                        //1、判断是否存在上一个记录文件，如果丢失，则创建一个新的文件进行记录
                        currentLogFileName = getCurrentErrorLogFileName();//非应用依赖的的文件
                        if (currentLogFileName == null) {
                            //直接创建新的日志文件--并记录
                            createNewLogFile();
                            saveLogIntoFile(currentLogFileName);
                        } else {
                            // 检查这个文件是否超出限定大小
                            File logFile = new File(currentLogFileName);
                            if (logFile.length() > LIMIT_LOG_FILE_SIZE) {
                                // 超出限定大小--------重新创建一个日志文件
                                createNewLogFile();
                                saveLogIntoFile(currentLogFileName);
                            } else {
                                //未超出大小限制，则在文件基础上进行追加
                                saveLogIntoFile(currentLogFileName);
                            }
                        }
                    }
                });
            }
        } else if (StorageUtil.externalMemoryAvailable() && IStorageInfo.SIZE_512M >= StorageUtil.getAvailableExternalMemorySize()) {
            throw new RuntimeException("Storage strategy applied,saving log info is forbidden when storage space less than 512M!");
        } else if (!StorageUtil.externalMemoryAvailable()) {
            throw new RuntimeException("External storage no available for saving log!");
        } else {
            throw new RuntimeException("Unknown error for saving log!");
        }
    }

    //************************************************************************************

    static class LOGHolder {
        private static SaveLogUtil instance = new SaveLogUtil();
    }
}
