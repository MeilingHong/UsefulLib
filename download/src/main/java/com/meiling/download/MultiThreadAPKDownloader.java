package com.meiling.download;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/21 0021.
 */
public class MultiThreadAPKDownloader extends AsyncTask<String,Long,Void> {
    /**
     * 可定义得更大
     */
    private final int MAX_SUBTHREAD = 3;
    private String netUrl;
    private String savePath;
    private List<SingleDownloadThread> subThreadList;

    private int sub_thread = 0;

    private boolean stopFlag;

    private int fileSize;
    private long downloadedSize;
    private IDownloadCallback icallback;
    private IDownloadErrorCallback iErrorcallback;

    private Dialog updateDialog;
    private Context activity;

    /**
     * TODO 修改点：
     * TODO 1、需要判断网络切换时（切换网络时会出现问题），是否需要提示，暂停下载
     * TODO 1.1、是否在允许在非WiFi网络状态下允许下载
     * TODO 2、若支持暂停下载，则需要下载支持断点续传
     * TODO 3、文件命名问题------如何建立一个有效的文件名映射规则
     */

    public MultiThreadAPKDownloader(Activity context, String netUrl, IDownloadCallback icallback, IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;
    }

    public MultiThreadAPKDownloader(Service context, String netUrl, IDownloadCallback icallback, IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;
    }

    public void start(){
        //TODO 需要检查是否拥有INTERNET权限，与读写权限
        //TODO 若需要引入，则compileSDKVersion 需要使用23 或以上版本
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(iErrorcallback!=null){
                iErrorcallback.noPermission(IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE);
            }
            return;
        }else if(ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            if(iErrorcallback!=null){
                iErrorcallback.noPermission(IErrorCode.NO_PERMISSION_INTERNET);
            }
            return;
        }else if(ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(iErrorcallback!=null){
                iErrorcallback.noPermission(IErrorCode.NO_PERMISSION_WRITE_EX_STORAGE);
            }
            return;
        }else if(ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            if(iErrorcallback!=null){
                iErrorcallback.noPermission(IErrorCode.NO_PERMISSION_ACCESS_NETWORK_STATE);
            }
            return;
        }
        execute("");
    }

    public void stop(){
        stopSubThread();
    }

    private void stopSubThread(){
        stopFlag = true;
        if(sub_thread==0 || subThreadList.size()==0){
            return;
        }
        for(int i = 0;i<sub_thread;i++){
            if(subThreadList!=null && subThreadList.get(i)!=null) {
                subThreadList.get(i).setBreakPointFlag(true);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        downloadedSize = 0;
        if(subThreadList!=null && subThreadList.size()>1){
            for(int i = 0;i<sub_thread;i++){
                if(subThreadList.get(i)!=null) {
                    downloadedSize += subThreadList.get(i).getDownloadLength();
                }
            }
        }else{
            downloadedSize = values[0];
        }

        /**
         * 更新UI显示的百分比
         */
        if(icallback!=null){
            icallback.updateProgress(((int)((downloadedSize*100)/fileSize)),((downloadedSize*100)/fileSize)+"%");
        }
    }

    @Override
    protected Void doInBackground(String... strings) {
//            publishProgress();
        try{
            URL url = new URL(netUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();
            int code = httpURLConnection.getResponseCode();
            for(int redirectCount = 0; code / 100 == 3 && redirectCount < 5; ++redirectCount) {
                httpURLConnection = this.createConnection(httpURLConnection.getHeaderField("Location"));
                code = httpURLConnection.getResponseCode();
            }

            if(code== HttpURLConnection.HTTP_OK || code== HttpURLConnection.HTTP_PARTIAL){
                fileSize = httpURLConnection.getContentLength();

                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+activity.getPackageName()+ File.separator+ FileDownloaderUtil.TEMP_DIR);
                if(!dir.exists()){
                    dir.mkdirs();
                }
                /**
                 *
                 */
                File apk = new File(dir, FileDownloaderUtil.getVersionApk());
                this.savePath = apk.getAbsolutePath();
                if(apk.exists()){
                    apk.renameTo(new File(dir,"temp"+FileDownloaderUtil.getVersionApk()));
                    apk.delete();
                    apk = new File(dir,FileDownloaderUtil.getVersionApk());
                    apk.createNewFile();
                    RandomAccessFile randomAccessFile = new RandomAccessFile(apk,"rwd");
                    randomAccessFile.setLength(fileSize);
                }

                int range = fileSize/sub_thread;
                for(int i = 0;i<sub_thread;i++){
                    if(i<sub_thread-1){
                        subThreadList.add(new SingleDownloadThread(netUrl, fileSize, sub_thread, i,
                                i * range, i * range + (range - 1), savePath, new IDownloadProgressCallback() {
                            @Override
                            public void threadProgress(int id,long progress) {
                                publishProgress(progress);
                            }
                        }));
                    }else{
                        subThreadList.add(new SingleDownloadThread(netUrl, fileSize, sub_thread, i,
                                i * range, fileSize, savePath, new IDownloadProgressCallback() {
                            @Override
                            public void threadProgress(int id,long progress) {
                                publishProgress(progress);
                            }
                        }));
                    }

                }
                for(int i = 0;i<sub_thread;i++){
                    subThreadList.get(i).start();
                }
                while (true) {
                    if((subThreadList.get(0)!=null? subThreadList.get(0).isAlive():true) &&
                            (subThreadList.get(1)!=null? subThreadList.get(1).isAlive():true) &&
                            (subThreadList.get(2)!=null? subThreadList.get(2).isAlive():true)
                            ){
                        break;
                    }else{

                    }
                }
                while (true) {
                    if((subThreadList.get(0)!=null? !subThreadList.get(0).isAlive():true) &&
                            (subThreadList.get(1)!=null? !subThreadList.get(1).isAlive():true) &&
                            (subThreadList.get(2)!=null? !subThreadList.get(2).isAlive():true)
                            ){
                        break;
                    }else{

                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            if(iErrorcallback!=null){
                iErrorcallback.noPermission(IErrorCode.ERROR_MALFORMEDURL);
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            if(iErrorcallback!=null){
                iErrorcallback.noPermission(IErrorCode.ERROR_PROTOCOL);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(iErrorcallback!=null){
                iErrorcallback.noPermission(IErrorCode.ERROR_SERVER_CONNECTION);
            }
        }
        return null;
    }

    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~\'%");
        HttpURLConnection conn = (HttpURLConnection)(new URL(encodedUrl)).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(20000);
        return conn;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(subThreadList.size()>0){
            if(stopFlag){
                if(updateDialog!=null && updateDialog.isShowing()){
                    updateDialog.dismiss();
                }
            }else{
                if(updateDialog!=null && updateDialog.isShowing()){
                    updateDialog.dismiss();
                }
                FileDownloaderUtil.updateAppVersion(activity);
                UtilTool.updateCheckTime(activity,netUrl);
            }
        }else{
            if(updateDialog!=null && updateDialog.isShowing()){
                updateDialog.dismiss();
            }
            if(iErrorcallback!=null){
                iErrorcallback.onError();
            }
        }
    }



}
