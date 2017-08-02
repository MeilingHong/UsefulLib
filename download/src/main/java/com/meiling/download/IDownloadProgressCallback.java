package com.meiling.download;

/**
 * Created by Administrator on 2016/7/15 0015.
 */
public interface IDownloadProgressCallback {

    public void threadProgress(int id, long progress);
}
