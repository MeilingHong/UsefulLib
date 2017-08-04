package com.meiling.usefullib;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.meiling.download.IDownloadCallback;
import com.meiling.download.IDownloadErrorCallback;
import com.meiling.download.IErrorCode;
import com.meiling.download.MultiThreadAPKDownloader;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class MainActivity extends AppCompatActivity {

    private boolean isStart = false;
    private TextView show;
    private MultiThreadAPKDownloader apkDownloader;


    private IDownloadErrorCallback errorCallback;
    private IDownloadCallback downloadCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        show = (TextView) findViewById(R.id.show);

        //TODO
        initCallback();

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  http://static.quanjiakan.com/familycare-download/apk/quanjiakanUser-release.apk
                // "http://p.gdown.baidu.com/da28deda0f8f3ed81f2f8360db97f932ea86b8bb4405bf6839aed9ff001be08de15582b63c16bfe2a85034000598d3c52683d64b856a1c8b1f46147f227e476d19a5ba74640361e5a99099cc7bd9d25952651f73c5d0e634d9c84964f9d93f576c70dc5af4eb8e52d13aeb11d1fda1fbd3fbb67699113e80056b257e424e2dce21cf61eabeebcaa8b74ee3e3972bc83095f3d9272cc26ff13282f6cabd2b2542441b89e8197f984c\n"
                if (isStart) {
                    isStart = false;
                    if (apkDownloader != null) {
                        apkDownloader.stop();
                    }
                } else {
                    isStart = true;
                    apkDownloader = new MultiThreadAPKDownloader(MainActivity.this
                            ,"http://p.gdown.baidu.com/da28deda0f8f3ed81f2f8360db97f932ea86b8bb4405bf6839aed9ff001be08de15582b63c16bfe2a85034000598d3c52683d64b856a1c8b1f46147f227e476d19a5ba74640361e5a99099cc7bd9d25952651f73c5d0e634d9c84964f9d93f576c70dc5af4eb8e52d13aeb11d1fda1fbd3fbb67699113e80056b257e424e2dce21cf61eabeebcaa8b74ee3e3972bc83095f3d9272cc26ff13282f6cabd2b2542441b89e8197f984c\n"
                            ,true
                            ,downloadCallback
                            ,errorCallback
                            ,null);
                    apkDownloader.start();
                }
            }
        });
    }

    public void initCallback(){
        errorCallback = new IDownloadErrorCallback() {
            @Override
            public void onError(int errorCode) {
                switch (errorCode) {
                    case IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE:
                        PermissionGen.with(MainActivity.this)
                                .addRequestCode(IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE)
                                .permissions(
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET
                                )
                                .request();
                        break;
                    case IErrorCode.NO_PERMISSION_INTERNET:
                        PermissionGen.with(MainActivity.this)
                                .addRequestCode(IErrorCode.NO_PERMISSION_INTERNET)
                                .permissions(
                                        Manifest.permission.INTERNET
                                )
                                .request();
                        break;
                    case IErrorCode.NO_PERMISSION_WRITE_EX_STORAGE:
                        PermissionGen.with(MainActivity.this)
                                .addRequestCode(IErrorCode.NO_PERMISSION_WRITE_EX_STORAGE)
                                .permissions(
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                                .request();
                        break;
                    case IErrorCode.NO_PERMISSION_ACCESS_NETWORK_STATE:
                        PermissionGen.with(MainActivity.this)
                                .addRequestCode(IErrorCode.NO_PERMISSION_ACCESS_NETWORK_STATE)
                                .permissions(
                                        Manifest.permission.ACCESS_NETWORK_STATE
                                )
                                .request();
                        break;
                    case IErrorCode.ERROR_MALFORMEDURL:
                        Toast.makeText(MainActivity.this, "URL 格式错误", Toast.LENGTH_SHORT).show();
                        break;
                    case IErrorCode.ERROR_PROTOCOL:
                        Toast.makeText(MainActivity.this, "协议异常", Toast.LENGTH_SHORT).show();
                        break;
                    case IErrorCode.ERROR_SERVER_CONNECTION:
                        Toast.makeText(MainActivity.this, "连接异常", Toast.LENGTH_SHORT).show();
                        break;
                    case IErrorCode.ERROR_NETWORK_DISABLE:
                        Toast.makeText(MainActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
                        break;
                    case IErrorCode.ERROR_NETWORK_MOBILE_FORBID_BY_USER:
                        Toast.makeText(MainActivity.this, "用户已禁止使用移动网络下载", Toast.LENGTH_SHORT).show();
                        break;
                    case IErrorCode.ERROR_NETWORK_CHANGE:
                        Toast.makeText(MainActivity.this, "网络切换", Toast.LENGTH_SHORT).show();
                        break;
                    case IErrorCode.ERROR_HTTP_ERROR:
                        Toast.makeText(MainActivity.this, "Http访问异常", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void noFinishDownload(int netType) {
                switch (netType){
                    case IErrorCode.ERROR_UNFINISH_DISABLE:
                        Toast.makeText(MainActivity.this, "网络不可用,导致下载终止!", Toast.LENGTH_SHORT).show();
                        break;
                    case IErrorCode.ERROR_UNFINISH_NORMAL:
                        Toast.makeText(MainActivity.this, "下载未完成!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void stopByUser() {
                Toast.makeText(MainActivity.this, "用户终止下载!", Toast.LENGTH_SHORT).show();
            }
        };

        //****************

        downloadCallback = new IDownloadCallback() {
            @Override
            public void updateProgress(int progress, String rate) {
                if (progress >= 100) {
                    isStart = false;
                    show.setText("完成");
                } else {
                    show.setText(rate);
                }
            }
        };
    }

    //***********************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //***********************************
    @PermissionSuccess(requestCode = IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE)
    public void INTERNET_WRITE_Yes() {
        Toast.makeText(this, "INTERNET_WRITE_Yes", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE)
    public void INTERNET_WRITE_No() {
        Toast.makeText(this, "INTERNET_WRITE_No", Toast.LENGTH_SHORT).show();
    }
    //***********************************
    @PermissionSuccess(requestCode = IErrorCode.NO_PERMISSION_INTERNET)
    public void INTERNET_Yes() {
        Toast.makeText(this, "INTERNET_Yes", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = IErrorCode.NO_PERMISSION_INTERNET)
    public void INTERNET_No() {
        Toast.makeText(this, "INTERNET_No", Toast.LENGTH_SHORT).show();
    }
    //***********************************
    @PermissionSuccess(requestCode = IErrorCode.NO_PERMISSION_WRITE_EX_STORAGE)
    public void WRITE_Yes() {
        Toast.makeText(this, "WRITE_Yes", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = IErrorCode.NO_PERMISSION_WRITE_EX_STORAGE)
    public void WRITE_No() {
        Toast.makeText(this, "WRITE_No", Toast.LENGTH_SHORT).show();
    }
    //***********************************
    @PermissionSuccess(requestCode = IErrorCode.NO_PERMISSION_ACCESS_NETWORK_STATE)
    public void ACCESS_NETWORK_Yes() {
        Toast.makeText(this, "ACCESS_NETWORK_Yes", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = IErrorCode.NO_PERMISSION_ACCESS_NETWORK_STATE)
    public void ACCESS_NETWORK_No() {
        Toast.makeText(this, "ACCESS_NETWORK_No", Toast.LENGTH_SHORT).show();
    }
}
