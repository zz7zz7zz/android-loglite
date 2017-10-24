package com.open.loglite;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    private String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.init(this,"log.config");
        Logger.v("A","MainActivity","onCreate");
//        log();
    }

    public void log(){
        StringBuilder sb = new StringBuilder(5000);
        for (int i = 1; i < 501; i++) {
            sb.append(String.format("%010d",i));
        }
        String str= sb.toString();
        int len = Log.v(TAG,str);
        Log.v(TAG,"---- len " + len);
        System.out.println("---- len " + len);
        System.out.println(str);
    }
}
