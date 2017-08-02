package com.meiling.usefullib;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.meiling.download.IDownloadCallback;
import com.meiling.download.IDownloadErrorCallback;
import com.meiling.download.IErrorCode;
import com.meiling.download.MultiThreadAPKDownloader;

import kr.co.namee.permissiongen.PermissionGen;

public class MainActivity extends AppCompatActivity {

    private boolean isStart = false;
    private TextView show;
    private MultiThreadAPKDownloader apkDownloader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        show = (TextView) findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  http://static.quanjiakan.com/familycare-download/apk/quanjiakanUser-release.apk
                if(isStart){
                    isStart = false;
                    if(apkDownloader!=null){
                        apkDownloader.stop();
                    }
                }else{
                    isStart = true;
                    apkDownloader = new MultiThreadAPKDownloader(MainActivity.this, "http://static.quanjiakan.com/familycare-download/apk/quanjiakanUser-release.apk",
                            new IDownloadCallback() {
                                @Override
                                public void updateProgress(int progress, String rate) {
                                    show.setText(rate);
                                }
                            }, new IDownloadErrorCallback() {
                        @Override
                        public void onError() {

                        }

                        @Override
                        public void noPermission(int type) {
                            switch (type){
                                case IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE:
                                    PermissionGen.with(MainActivity.this)
                                            .addRequestCode(IErrorCode.NO_PERMISSION_WRITE_EX_STORAGE)
                                            .permissions(
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            )
                                            .request();
                                    PermissionGen.with(MainActivity.this)
                                            .addRequestCode(IErrorCode.NO_PERMISSION_INTERNET)
                                            .permissions(
                                                    Manifest.permission.INTERNET
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
                            }
                        }
                    }, null);
                    apkDownloader.start();
                }
            }
        });
    }
}
