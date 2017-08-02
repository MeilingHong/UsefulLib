package com.meiling.download;

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
    private String url_string;

    private long sumSize;
    private int sumThreadNumber;
    private int threadNumber;

    private long start;
    private long end;
    private String savePath;

    private long currentPosition;

    private boolean breakPointFlag = false;

    public SingleDownloadThread(String filePath, long fileSumSize, int sumThreadNumber, int threadNumber,
                                long startPosition, long endPosition, String localSavePath, IDownloadProgressCallback callback){
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

    public void setBreakPointFlag(boolean flag){
        breakPointFlag = flag;
    }

    public long getDownloadLength(){
        return currentPosition - start;
    }

    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~\'%");
        HttpURLConnection conn = (HttpURLConnection)(new URL(encodedUrl)).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(20000);
        return conn;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(url_string);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(METHOD_GET);
            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.addRequestProperty("Range", "bytes="+start+"-"+end);
            httpURLConnection.connect();

            /**
             * TODO 若地址出现重定向，则在进行有限次数的重定向处理
             */
            int code = httpURLConnection.getResponseCode();
            for(int redirectCount = 0; code / 100 == 3 && redirectCount < 5; ++redirectCount) {
                httpURLConnection = this.createConnection(httpURLConnection.getHeaderField("Location"));
                code = httpURLConnection.getResponseCode();
            }


            if(code== HttpURLConnection.HTTP_OK || code== HttpURLConnection.HTTP_PARTIAL){
                int streamSize = httpURLConnection.getContentLength();
                if(streamSize>=sumSize){
                    /**
                     * 不支持多线程下载
                     */
                    if(threadNumber>0){
                        httpURLConnection.getInputStream().close();
                        httpURLConnection.disconnect();
                        return;
                    }else{
                        start = 0;
                        end = httpURLConnection.getContentLength();
                    }
                }else{
                    /**
                     * 支持多线程下载
                     */
                }

                File tempFile = new File(savePath);
                RandomAccessFile file = new RandomAccessFile(savePath,"rwd");
                if(!tempFile.exists()){
                    tempFile.createNewFile();
                    file.setLength(sumSize);
                }
                file.seek(currentPosition);


                InputStream inputStream = httpURLConnection.getInputStream();
                /**
                 * 若需要断点续传，则需要保存当前
                 */
                byte[] cache = new byte[4096];//缓存
                int len = 0;
                long tempSum = 0;

                while((len = inputStream.read(cache))!=-1){

                    currentPosition+=len;
                    tempSum+=len;
                    file.write(cache,0,len);
                    if(callback!=null){
                        callback.threadProgress(threadNumber,tempSum);
                    }
                }

                file.close();
                inputStream.close();
            }else{

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
