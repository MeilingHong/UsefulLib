package com.meiling.download;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

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
 *
 * 下载指定文件并将文件打开
 *
 * Created by Administrator on 2016/9/21 0021.
 */
public class MultiThreadFileDownloader extends AsyncTask<String,Long,Integer> {
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
    private boolean isAllowMobileNetwork;

    public MultiThreadFileDownloader(Activity context, String netUrl, IDownloadCallback icallback, IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;

        isAllowMobileNetwork = false;
    }

    public MultiThreadFileDownloader(Activity context, String netUrl, boolean isAllowMobile, IDownloadCallback icallback, IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;

        isAllowMobileNetwork = isAllowMobile;
    }

    public MultiThreadFileDownloader(Service context, String netUrl, IDownloadCallback icallback, IDownloadErrorCallback iErrorcallback, Dialog dialog){
        sub_thread = MAX_SUBTHREAD;
        this.netUrl = netUrl;
        subThreadList = new ArrayList<SingleDownloadThread>();
        this.icallback = icallback;
        this.iErrorcallback = iErrorcallback;
        this.updateDialog = dialog;
        activity = context;

        isAllowMobileNetwork = false;
    }

    public void start(){
        execute("");
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

    public void stop(){
        stopSubThread();
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
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();
            //  当网址存在重定向时，使用重定向
            int code = httpURLConnection.getResponseCode();
            for(int redirectCount = 0; code / 100 == 3 && redirectCount < 5; ++redirectCount) {
                httpURLConnection = this.createConnection(httpURLConnection.getHeaderField("Location"));
                code = httpURLConnection.getResponseCode();
            }


            if(httpURLConnection.getResponseCode()== HttpURLConnection.HTTP_OK){
                fileSize = httpURLConnection.getContentLength();


                File dir = new File(FileDownloaderUtil.getRootCacheDirectory(activity));
                if(!dir.exists()){
                    dir.mkdirs();
                }
                /**
                 * 每一次都重新下载
                 */
//                final String fileName = netUrl.substring(netUrl.lastIndexOf("/")+1);
//                File apk = new File(dir, fileName);
//                this.savePath = apk.getAbsolutePath();
//                if(apk.exists()){
//                    apk.renameTo(new File(dir,"temp_"+fileName));
//                    apk.delete();
//                    apk = new File(dir,fileName);
//                    apk.createNewFile();
//                    RandomAccessFile randomAccessFile = new RandomAccessFile(apk,"rwd");
//                    randomAccessFile.setLength(fileSize);
//                }
                /**
                 * 进行断点下载
                 */
                final String fileName = FileNameHash.getSHA1String(netUrl+fileSize)//  用于防止重复---（实际上仍存在重复的可能，虽然几率比较小）
                        +netUrl.substring(netUrl.lastIndexOf("/")+1);
                File apk = new File(dir, fileName);
                this.savePath = apk.getAbsolutePath();
                if(!apk.exists()){
//                    apk.renameTo(new File(dir,"temp_"+fileName));
//                    apk.delete();
//                    apk = new File(dir,fileName);
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
                        }));
                    }else{
                        subThreadList.add(new SingleDownloadThread(activity,netUrl, fileSize, sub_thread, i,
                                i * range, fileSize, savePath,isAllowMobileNetwork, new IDownloadProgressCallback() {
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
//                if(subThreadList.size()>0 && subThreadList.get(0)!=null){
//                    subThreadList.get(0).start();
//                }
//                if(subThreadList.size()>1 && subThreadList.get(1)!=null){
//                    subThreadList.get(1).start();
//                }
//                if(subThreadList.size()>2 && subThreadList.get(2)!=null){
//                    subThreadList.get(2).start();
//                }


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
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
            if(savePath.toLowerCase().endsWith("doc") || savePath.toLowerCase().endsWith("docx")){
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(new File(savePath)), "application/msword");
                activity.startActivity(intent);
            }else{
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.fromFile(new File(savePath)), "*/*");
                activity.startActivity(intent);
            }
//            UpdateUtil.updateCheckTime(activity,netUrl);
            return;
        }

        if(subThreadList.size()>0){
            if(stopFlag){
                if(updateDialog!=null && updateDialog.isShowing()){
                    updateDialog.dismiss();
                }
            }else{
                if(updateDialog!=null && updateDialog.isShowing()){
                    updateDialog.dismiss();
                }
//                FileDownloaderUtil.updateAppVersion(activity);
//                BaseApplication.getInstances().updateCheckTime();
                /**
                 * 调用外部APP打开文件
                 */
                if(downloadedSize<fileSize){
                    if(iErrorcallback!=null){
                        iErrorcallback.noFinishDownload(IErrorCode.ERROR_UNFINISH_DISABLE);
                    }
                }else{
                    if(savePath.toLowerCase().endsWith("doc") || savePath.toLowerCase().endsWith("docx")){
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(new File(savePath)), "application/msword");
                        activity.startActivity(intent);
                    }else{
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setDataAndType(Uri.fromFile(new File(savePath)), "*/*");
                        activity.startActivity(intent);
                    }
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
