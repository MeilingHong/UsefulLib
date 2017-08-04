package com.meiling.download;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2016/7/15 0015.
 */
public class SingleDownloadThread extends Thread {

    private final String METHOD_GET = "GET";

    private IDownloadProgressCallback callback;
    private IDownloadErrorCallback errorCallback;

    private int currentNetworkType;
    private int sumThreadNumber;
    private int threadNumber;
    private long sumSize;
    private long start;
    private long end;
    private long currentPosition;
    private boolean breakPointFlag = false;
    private String savePath;
    private String url_string;

    private Context mContext;

    public SingleDownloadThread(Context mContext, String filePath, long fileSumSize, int sumThreadNumber, int threadNumber,
                                long startPosition, long endPosition, String localSavePath, IDownloadProgressCallback callback) {
        this.mContext = mContext;
        url_string = filePath;
        sumSize = fileSumSize;
        this.sumThreadNumber = sumThreadNumber;
        this.threadNumber = threadNumber;
        start = startPosition;
        end = endPosition;
        currentPosition = startPosition;
        this.callback = callback;
        savePath = localSavePath;
    }

    public SingleDownloadThread(Context mContext, String filePath, long fileSumSize, int sumThreadNumber, int threadNumber,
                                long startPosition, long endPosition, String localSavePath, IDownloadProgressCallback callback
            , IDownloadErrorCallback errorCallback) {
        this.mContext = mContext;
        url_string = filePath;
        sumSize = fileSumSize;
        this.sumThreadNumber = sumThreadNumber;
        this.threadNumber = threadNumber;
        start = startPosition;
        end = endPosition;
        currentPosition = startPosition;
        this.callback = callback;
        savePath = localSavePath;
        this.errorCallback = errorCallback;
    }

    public void setBreakPointFlag(boolean flag) {
        breakPointFlag = flag;
    }

    public long getDownloadLength() {
        return currentPosition - start;
    }

    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~\'%");
        HttpURLConnection conn = (HttpURLConnection) (new URL(encodedUrl)).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(20000);
        return conn;
    }

    @Override
    public void run() {
        //TODO ******************************
//        RandomAccessFile file = null;
//        InputStream inputStream = null;
//        try {
//            currentNetworkType = NetCheckUtil.checkNetworkType(mContext);
//            URL url = new URL(url_string);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod(METHOD_GET);
//            httpURLConnection.setDoInput(true);
////            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setConnectTimeout(5000);
//            httpURLConnection.addRequestProperty("Range", "bytes="+start+"-"+end);
//            httpURLConnection.connect();
//
//            /**
//             * TODO 若地址出现重定向，则在进行有限次数的重定向处理
//             */
//            int code = httpURLConnection.getResponseCode();
//            for(int redirectCount = 0; code / 100 == 3 && redirectCount < 5; ++redirectCount) {
//                httpURLConnection = this.createConnection(httpURLConnection.getHeaderField("Location"));
//                code = httpURLConnection.getResponseCode();
//            }
//            if(code== HttpURLConnection.HTTP_OK || code== HttpURLConnection.HTTP_PARTIAL){
//                int streamSize = httpURLConnection.getContentLength();
//                if(streamSize>=sumSize){
//                    /**
//                     * 不支持多线程下载
//                     */
//                    if(threadNumber>0){
//                        httpURLConnection.getInputStream().close();
//                        httpURLConnection.disconnect();
//                        return;
//                    }else{
//                        start = 0;
//                        end = httpURLConnection.getContentLength();
//                    }
//                }else{
//                    /**
//                     * 支持多线程下载
//                     */
//                }
//
//                File tempFile = new File(savePath);
//                file = new RandomAccessFile(savePath,"rwd");
//                if(!tempFile.exists()){
//                    tempFile.createNewFile();
//                    file.setLength(sumSize);
//                }
//                file.seek(currentPosition);
//
//
//                inputStream = httpURLConnection.getInputStream();
//                /**
//                 * 若需要断点续传，则需要保存当前
//                 */
//                byte[] cache = new byte[4096];//缓存
//                int len = 0;
//                long tempSum = 0;
//
//                while((len = inputStream.read(cache))!=-1){
//
//                    currentPosition+=len;
//                    tempSum+=len;
//                    file.write(cache,0,len);
//                    if(callback!=null){
//                        callback.threadProgress(threadNumber,tempSum);
//                    }
//                }
//            }else{
//            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            Log.e("MainA","MalformedURLException   run "+sumThreadNumber+"      "+url_string);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e("MainA","IOException   run "+sumThreadNumber+"      "+url_string);
//            if(currentNetworkType!=NetCheckUtil.checkNetworkType(mContext)){
//                //TODO 当网络环境改变
//                reDownload();
//            }else{
//            }
//        } finally {
//            try {
//                if(file!=null){
//                    file.close();
//                }
//                if(inputStream!=null){
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        //TODO
        reDownload();
    }

    public void download() {
        RandomAccessFile file = null;
        InputStream inputStream = null;
        try {
            currentNetworkType = NetCheckUtil.checkNetworkType(mContext);
            URL url = new URL(url_string);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(METHOD_GET);
            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.addRequestProperty("Range", "bytes=" + currentPosition + "-" + end);
            httpURLConnection.connect();

            /**
             * TODO 若地址出现重定向，则在进行有限次数的重定向处理
             */
            int code = httpURLConnection.getResponseCode();
            for (int redirectCount = 0; code / 100 == 3 && redirectCount < 5; ++redirectCount) {
                httpURLConnection = this.createConnection(httpURLConnection.getHeaderField("Location"));
                code = httpURLConnection.getResponseCode();
            }
            if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL) {
                int streamSize = httpURLConnection.getContentLength();
                if (streamSize >= sumSize) {
                    /**
                     * 不支持多线程下载
                     */
                    if (threadNumber > 0) {
                        httpURLConnection.getInputStream().close();
                        httpURLConnection.disconnect();
                        return;
                    } else {
                        start = 0;
                        end = httpURLConnection.getContentLength();
                    }
                } else {
                    /**
                     * 支持多线程下载
                     */
                }

                File tempFile = new File(savePath);
                file = new RandomAccessFile(savePath, "rwd");
                if (!tempFile.exists()) {
                    tempFile.createNewFile();
                    file.setLength(sumSize);
                }
                file.seek(currentPosition);


                inputStream = httpURLConnection.getInputStream();
                /**
                 * 若需要断点续传，则需要保存当前
                 */
                byte[] cache = new byte[4096];//缓存
                int len = 0;
                long tempSum = 0;

                while ((len = inputStream.read(cache)) != -1) {

                    currentPosition += len;
                    tempSum += len;
                    file.write(cache, 0, len);
                    if (callback != null) {
                        callback.threadProgress(threadNumber, tempSum);
                    }
                }
            } else {
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("MainA", "MalformedURLException   reDownload " + threadNumber + "      " + url_string);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainA", "IOException   reDownload " + threadNumber + "      " + url_string);
            if ((currentNetworkType != NetCheckUtil.checkNetworkType(mContext) &&
                    NetCheckUtil.TYPE_MOBILE==NetCheckUtil.checkNetworkType(mContext)) ||
                    NetCheckUtil.TYPE_WIFI==NetCheckUtil.checkNetworkType(mContext)) {
                //TODO 当网络环境改变
                reDownload();
                try {
                    if (file != null) {
                        file.close();
                        file = null;
                    }
                    if (inputStream != null) {
                        inputStream.close();
                        inputStream = null;
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                Log.e("MainA", "return ----- redo    " + NetCheckUtil.checkNetworkType(mContext));
                return;
            } else {
                Log.e("MainA", "IOException   reDownload  undo    " + NetCheckUtil.checkNetworkType(mContext));
            }
        } finally {
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void reDownload() {
        RandomAccessFile file = null;
        InputStream inputStream = null;
        try {
            currentNetworkType = NetCheckUtil.checkNetworkType(mContext);
            URL url = new URL(url_string);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(METHOD_GET);
            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.addRequestProperty("Range", "bytes=" + currentPosition + "-" + end);
            httpURLConnection.connect();

            /**
             * TODO 若地址出现重定向，则在进行有限次数的重定向处理
             */
            int code = httpURLConnection.getResponseCode();
            for (int redirectCount = 0; code / 100 == 3 && redirectCount < 5; ++redirectCount) {
                httpURLConnection = this.createConnection(httpURLConnection.getHeaderField("Location"));
                code = httpURLConnection.getResponseCode();
            }
            if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL) {
                int streamSize = httpURLConnection.getContentLength();
                if (streamSize >= sumSize) {
                    /**
                     * 不支持多线程下载
                     */
                    if (threadNumber > 0) {
                        httpURLConnection.getInputStream().close();
                        httpURLConnection.disconnect();
                        return;
                    } else {
                        start = 0;
                        end = httpURLConnection.getContentLength();
                    }
                } else {
                    /**
                     * 支持多线程下载
                     */
                }

                File tempFile = new File(savePath);
                file = new RandomAccessFile(savePath, "rwd");
                if (!tempFile.exists()) {
                    tempFile.createNewFile();
                    file.setLength(sumSize);
                }
                file.seek(currentPosition);


                inputStream = httpURLConnection.getInputStream();
                /**
                 * 若需要断点续传，则需要保存当前
                 */
                byte[] cache = new byte[4096];//缓存
                int len = 0;
                long tempSum = 0;

                while ((len = inputStream.read(cache)) != -1) {

                    currentPosition += len;
                    tempSum += len;
                    file.write(cache, 0, len);
                    if (callback != null) {
                        callback.threadProgress(threadNumber, tempSum);
                    }
                }
            } else {
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("MainA", "MalformedURLException   reDownload " + threadNumber + "      " + url_string);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainA", "IOException   reDownload " + threadNumber + "      " + url_string);
            if ((currentNetworkType != NetCheckUtil.checkNetworkType(mContext) &&
                    NetCheckUtil.TYPE_MOBILE==NetCheckUtil.checkNetworkType(mContext)) ||
                    NetCheckUtil.TYPE_WIFI==NetCheckUtil.checkNetworkType(mContext)) {
                //TODO 当网络环境改变
                download();
                try {
                    if (file != null) {
                        file.close();
                        file = null;
                    }
                    if (inputStream != null) {
                        inputStream.close();
                        inputStream = null;
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                Log.e("MainA", "return  +++++  redo    " + NetCheckUtil.checkNetworkType(mContext));
                return;
            } else {
                Log.e("MainA", "IOException   download  undo    " + NetCheckUtil.checkNetworkType(mContext));
            }
        } finally {
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
