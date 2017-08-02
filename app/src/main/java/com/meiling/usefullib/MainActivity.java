package com.meiling.usefullib;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private boolean aBoolean = false;
    private TextView show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        show = (TextView) findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  http://static.quanjiakan.com/familycare-download/apk/quanjiakanUser-release.apk
                if(aBoolean){
                    aBoolean = false;
                }else{
                    aBoolean = true;
                }
            }
        });
    }
}
