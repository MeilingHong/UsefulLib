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
                // "http://p.gdown.baidu.com/da28deda0f8f3ed81f2f8360db97f932ea86b8bb4405bf6839aed9ff001be08de15582b63c16bfe2a85034000598d3c52683d64b856a1c8b1f46147f227e476d19a5ba74640361e5a99099cc7bd9d25952651f73c5d0e634d9c84964f9d93f576c70dc5af4eb8e52d13aeb11d1fda1fbd3fbb67699113e80056b257e424e2dce21cf61eabeebcaa8b74ee3e3972bc83095f3d9272cc26ff13282f6cabd2b2542441b89e8197f984c\n"
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

                                    if(progress>=100){
                                        isStart = false;
                                        show.setText("完成");
                                    }else{
                                        show.setText(rate);
                                    }
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
                                            .addRequestCode(IErrorCode.NO_PERMISSION_INTERNET_WRITE_EX_STORAGE)
                                            .permissions(
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET
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
                                case IErrorCode.ERROR_MALFORMEDURL:
                                    Toast.makeText(MainActivity.this, "URL 格式错误", Toast.LENGTH_SHORT).show();
                                    break;
                                case IErrorCode.ERROR_PROTOCOL:
                                    Toast.makeText(MainActivity.this, "协议异常", Toast.LENGTH_SHORT).show();
                                    break;
                                case IErrorCode.ERROR_SERVER_CONNECTION:
                                    Toast.makeText(MainActivity.this, "连接异常", Toast.LENGTH_SHORT).show();
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
