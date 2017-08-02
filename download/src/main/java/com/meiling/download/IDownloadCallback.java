package com.meiling.download;

/**
 * Created by Administrator on 2016/9/21 0021.
 */
public interface IDownloadCallback {
    void updateProgress(int progress, String rate);
}
