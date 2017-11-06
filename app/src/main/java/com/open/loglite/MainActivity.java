package com.open.loglite;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.open.loglite.base.Config;

import java.io.File;

public class MainActivity extends Activity {

    private String TAG = "MainActivity";
    static boolean isInited = false;
    static int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isInited){
            Config mConfig = Logger.init(this,"log_config",getDiskCacheDir(this));
            System.out.println(mConfig);
            isInited = true;
        }

        findViewById(R.id.log_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Logger.v("A",TAG,"onCreate " + (++count));
                Logger.v("C",TAG,"onCreate " + (++count));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.v("B",TAG,"Thread run " + (++count));
                        Logger.v("D",TAG,"Thread run " + (++count));
                    }
                }).start();

//                StringBuilder sb = new StringBuilder(5000);
//                for (int i = 1; i < 502; i++) {
//                    sb.append(String.format("%010d",i));
//                }
//                String str= sb.toString();
//                int len = Log.v(TAG,str);
////        Log.v(TAG,"---- len " + len);
////        System.out.println("---- len " + len);
////        System.out.println(str);
//
//                Logger.v("A",TAG,str);

            }
        });
    }


    public static String getDiskCacheDir(Context mContext) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(mContext, null);
            if (externalFilesDirs != null && externalFilesDirs.length > 0 && externalFilesDirs[0] != null) {
                cachePath = externalFilesDirs[0].getAbsolutePath();
            } else {
                cachePath = mContext.getCacheDir().getPath();
            }
        } else {
            cachePath = mContext.getCacheDir().getPath();
        }
        return cachePath + File.separator;
    }
}
