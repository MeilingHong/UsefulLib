package com.meiling.download;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
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
public class MultiThreadAPKDownloader extends AsyncTask<String,Long,Integer> {
    /**
     * 可定义得更大
     */
    private final int MAX_SUBTHREAD = 3;

    private boolean stopFlag;
    private boolean isAllowMobileNetwork;//是否允许移动网络环境下下载
    private int sub_thread = 0;
    private int currentNetworkType;
    private int fileSize;
    private long downloadedSize;
    private String netUrl;
    private String savePath;
    private List<SingleDownloadThread> subThreadList;
    private IDownloadCallback icallback;
    private IDownloadErrorCallback iErrorcallback;
    private Dialog updateDialog;
    private Context activity;

    /**
     *   修改点：
     *   1、需要判断网络切换时（切换网络时会出现问题），是否需要提示，暂停下载
     *   1.1、是否在允许在非WiFi网络状态下允许下载
     *   2、若支持暂停下载，则需要下载支持断点续传
     *   3、文件命名问题------如何建立一个有效的文件名映射规则
     */

    public MultiThreadAPKDownloader(Activity context, String netUrl, IDownloadCallback icallback, IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;
        //  默认不允许使用移动网络进行下载
        isAllowMobileNetwork = false;
    }

    public MultiThreadAPKDownloader(Activity context, String netUrl,boolean isMobileNet, IDownloadCallback icallback,
                                    IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;
        //  根据配置进行规制进行下载，当允许时，切换为
        isAllowMobileNetwork = isMobileNet;
    }

    public MultiThreadAPKDownloader(Service context, String netUrl,boolean isMobileNet, IDownloadCallback icallback, IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;

        //  根据配置进行规制进行下载，当允许时，切换为
        isAllowMobileNetwork = isMobileNet;
    }

    public void start(){
        //  需要检查是否拥有INTERNET权限，与读写权限
        //  若需要引入，则compileSDKVersion 需要使用23 或以上版本
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(iErrorcallback!=null){
                iErrorcallback.onError(IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE);
            }
            return;
        }else if(ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            if(iErrorcallback!=null){
                iErrorcallback.onError(IErrorCode.NO_PERMISSION_INTERNET);
            }
            return;
        }else if(ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(iErrorcallback!=null){
                iErrorcallback.onError(IErrorCode.NO_PERMISSION_WRITE_EX_STORAGE);
            }
            return;
        }else if(ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            if(iErrorcallback!=null){
                iErrorcallback.onError(IErrorCode.NO_PERMISSION_ACCESS_NETWORK_STATE);
            }
            return;
        }
        //***********************************************************************
        //获取当前网络环境
        currentNetworkType = NetCheckUtil.checkNetworkType(activity);
        if(currentNetworkType==NetCheckUtil.TYPE_NET_WORK_DISABLED || currentNetworkType==NetCheckUtil.TYPE_OTHER){
            if(iErrorcallback!=null){
                iErrorcallback.onError(IErrorCode.ERROR_NETWORK_DISABLE);
            }
            return;
        }else if(currentNetworkType==NetCheckUtil.TYPE_MOBILE && !isAllowMobileNetwork){
            if(iErrorcallback!=null){
                iErrorcallback.onError(IErrorCode.ERROR_NETWORK_MOBILE_FORBID_BY_USER);
            }
            return;
        }
        //  允许进行下载的网络
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

        /**
         *   覆盖文件
         */
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
//        if(icallback!=null){
//            icallback.updateProgress(((int)((downloadedSize*100)/fileSize)),((downloadedSize*100)/fileSize)+"%");
//        }

        /**
         *   使用相同的文件
         */
//        Log.e("MainA","-------\n" +
//                "downloadedSize:"+downloadedSize+"     ---fileSize："+fileSize);
        if(icallback!=null){
            icallback.updateProgress(((int)((downloadedSize*100)/fileSize)),((downloadedSize*100)/fileSize)+"%");
        }
    }

    @Override
    protected Integer doInBackground(String... strings) {
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
//                Log.e("MainA","fileSize:"+fileSize);
                File dir = new File(FileDownloaderUtil.getRootCacheDirectory(activity));
                if(!dir.exists()){
                    dir.mkdirs();
                }
                /**
                 *   每次下载覆盖重复的文件
                 */
//                File apk = new File(dir, FileNameHash.getSHA1String(netUrl+fileSize)+".apk");
//                this.savePath = apk.getAbsolutePath();
//                if(apk.exists()){
//                    apk.renameTo(new File(dir,"temp"+FileNameHash.getSHA1String(netUrl+fileSize)+".apk"));
//                    apk.delete();
//                    apk = new File(dir,FileNameHash.getSHA1String(netUrl+fileSize)+".apk");
//                    apk.createNewFile();
//                    RandomAccessFile randomAccessFile = new RandomAccessFile(apk,"rwd");
//                    randomAccessFile.setLength(fileSize);
//                }

                /**
                 *   使用相同的文件
                 */
                File apk = new File(dir, FileNameHash.getSHA1String(netUrl+fileSize)+".apk");
                this.savePath = apk.getAbsolutePath();
                if(!apk.exists()){
//                    apk.renameTo(new File(dir,"temp"+FileNameHash.getSHA1String(netUrl+fileSize)+".apk"));
//                    apk.delete();
//                    apk = new File(dir,FileNameHash.getSHA1String(netUrl+fileSize)+".apk");
                    apk.createNewFile();
                    //  若文件不存在了，则需要创建文件，而且同步清除该文件的断点记录
                    for(int i = 0;i<sub_thread;i++){
                        String tempInfo = UpdateUtil.getFileInfoValue(activity,FileNameHash.getSHA1String(netUrl+fileSize+"_"+i));//  在保存和获取断点数据时，必须根据子线程来，所以Key中必须带有子线程编号
                        if(tempInfo!=null &&//  不为空
                                !"".equals(tempInfo) && //  不为默认值
                                tempInfo.split(UpdateUtil.SPLIT).length==6) {
                            //  存在则清除这个数据--------保持数据上的同步（以下载的文件为主）
                            UpdateUtil.setFileInfoValue(activity,FileNameHash.getSHA1String(netUrl+fileSize+"_"+i),
                                    "");
                        }
                    }
                }else{
                    int count = 0;
                    for(int i = 0;i<sub_thread;i++){
                        String tempInfo = UpdateUtil.getFileInfoValue(activity,FileNameHash.getSHA1String(netUrl+fileSize+"_"+i));//  在保存和获取断点数据时，必须根据子线程来，所以Key中必须带有子线程编号
                        if(tempInfo!=null &&//  不为空
                                !"".equals(tempInfo) && //  不为默认值
                                tempInfo.split(UpdateUtil.SPLIT).length==6) {
                            //  存在则清除这个数据--------保持数据上的同步（以下载的文件为主）
                            long currentPosition = Long.parseLong(tempInfo.split(UpdateUtil.SPLIT)[5]);
                            long end = Long.parseLong(tempInfo.split(UpdateUtil.SPLIT)[4]);
                            if(currentPosition>=end){
                                count++;
                            }
                        }
                    }
                    if(count==sub_thread){
                        return IErrorCode.CODE_NORMAL_FINISH_DOWNLOAD;
                    }
                }

                RandomAccessFile randomAccessFile = new RandomAccessFile(apk,"rwd");
                randomAccessFile.setLength(fileSize);

                int range = fileSize/sub_thread;
                for(int i = 0;i<sub_thread;i++){
                    if(i<sub_thread-1){
                        subThreadList.add(new SingleDownloadThread(activity,netUrl, fileSize, sub_thread, i,
                                i * range, i * range + (range - 1), savePath,isAllowMobileNetwork, new IDownloadProgressCallback() {
                            @Override
                            public void threadProgress(int id,long progress) {
                                publishProgress(progress);
                            }
                        },iErrorcallback));
                    }else{
                        subThreadList.add(new SingleDownloadThread(activity,netUrl, fileSize, sub_thread, i,
                                i * range, fileSize, savePath,isAllowMobileNetwork, new IDownloadProgressCallback() {
                            @Override
                            public void threadProgress(int id,long progress) {
                                publishProgress(progress);
                            }
                        },iErrorcallback));
                    }

                }

//                for(int i = 0;i<sub_thread;i++){
//                    subThreadList.get(i).start();
//                }

                if(subThreadList.size()>0 && subThreadList.get(0)!=null){
                    subThreadList.get(0).start();
                }
                if(subThreadList.size()>1 && subThreadList.get(1)!=null){
                    subThreadList.get(1).start();
                }
                if(subThreadList.size()>2 && subThreadList.get(2)!=null){
                    subThreadList.get(2).start();
                }


                //  保证子线程是启动的
                while (true) {
                    if((subThreadList.get(0)!=null? subThreadList.get(0).isAlive():true) &&
                            (subThreadList.get(1)!=null? subThreadList.get(1).isAlive():true) &&
                            (subThreadList.get(2)!=null? subThreadList.get(2).isAlive():true)
                            ){
                        break;
                    }else{
                    }

                }

                //  保证子线程完成后再退出
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
//            if(iErrorcallback!=null){
//                iErrorcallback.onError(IErrorCode.ERROR_MALFORMEDURL);
//            }
        } catch (ProtocolException e) {
            e.printStackTrace();
//            if(iErrorcallback!=null){
//                iErrorcallback.onError(IErrorCode.ERROR_PROTOCOL);
//            }
        } catch (IOException e) {
            e.printStackTrace();
//            if(iErrorcallback!=null){
//                iErrorcallback.onError(IErrorCode.ERROR_SERVER_CONNECTION);
//            }
        }
        return null;
    }

    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~\'%");
        netUrl = encodedUrl;
        HttpURLConnection conn = (HttpURLConnection)(new URL(encodedUrl)).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(20000);
        return conn;
    }

    @Override
    protected void onPostExecute(Integer aVoid) {
        super.onPostExecute(aVoid);
        if(aVoid!=null && IErrorCode.CODE_NORMAL_FINISH_DOWNLOAD==aVoid.intValue()){
            if(icallback!=null){
                icallback.updateProgress(100,"100%");
            }
            FileDownloaderUtil.updateAppVersion(activity,FileNameHash.getSHA1String(netUrl+fileSize)+".apk");
//            UpdateUtil.updateCheckTime(activity,netUrl);
            return;
        }
        if(subThreadList!=null && subThreadList.size()>0){
            if(stopFlag){
                if(updateDialog!=null && updateDialog.isShowing()){
                    updateDialog.dismiss();
                }
                if(iErrorcallback!=null){
                    iErrorcallback.stopByUser();
                }
            }else{
                if(updateDialog!=null && updateDialog.isShowing()){
                    updateDialog.dismiss();
                }

                if(downloadedSize<fileSize){
                    if(iErrorcallback!=null){
                        iErrorcallback.noFinishDownload(IErrorCode.ERROR_UNFINISH_DISABLE);
                    }
                }else{
                    FileDownloaderUtil.updateAppVersion(activity,FileNameHash.getSHA1String(netUrl+fileSize)+".apk");
                    UpdateUtil.updateCheckTime(activity,netUrl);
                }
            }
        }else{
            if(updateDialog!=null && updateDialog.isShowing()){
                updateDialog.dismiss();
            }
            if(iErrorcallback!=null){
                iErrorcallback.noFinishDownload(IErrorCode.ERROR_UNFINISH_NORMAL);
            }
        }
    }



}
