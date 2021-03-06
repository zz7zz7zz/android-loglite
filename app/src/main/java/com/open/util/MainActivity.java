package com.open.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.open.util.log.Logger;
import com.open.util.log.base.LogConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends Activity {

    private String TAG = "MainActivity";
    static int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogConfig mConfig = Logger.init(this, "log.config",getDiskCacheDir(this));

        System.out.println("-------------------------------");
        System.out.println(mConfig);
        System.out.println("-------------------------------");

        findViewById(R.id.log_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //-------------------------1.测试字符串-------------------------------
                Logger.v("onCreate A " + (++count));//强制写
                Logger.vt(TAG,"onCreate B " + (++count));//强制写
                Logger.vat(LogAutor.AUTHOR_A,TAG,"onCreate C " + (++count));
//                Logger.vat(LogAutor.AUTHOR_B,TAG,"onCreate " + (++count));
//                Logger.vatd(LogAutor.AUTHOR_C,TAG,"onCreate " + (++count));

//                //-------------------------2.测试JSON对象-------------------------------
                try {
                    JSONObject mJSONObject = new JSONObject();
                    mJSONObject.put("name","yang");
                    mJSONObject.put("age",18);

                    Logger.vat(LogAutor.AUTHOR_A,TAG,mJSONObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//
//                //-------------------------3.测试JSON数组-------------------------------
//                try {
//                    JSONArray mJSONArray = new JSONArray();
//                    for (int i = 0;i<5;i++){
//                        JSONObject mJSONObject = new JSONObject();
//                        mJSONObject.put("name","yang " + i);
//                        mJSONObject.put("age",18+i);
//                        mJSONArray.put(mJSONObject);
//                    }
//
//                    Logger.v(LogAutor.AUTHOR_A,TAG,mJSONArray);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                //-------------------------4.测试异常-------------------------------
//                try {
//                    String str = null;
//                    str.equals("null");
//                } catch (Exception e) {
////                    e.printStackTrace();
//                    Logger.v(LogAutor.AUTHOR_A,TAG,e);
//                }

//                Logger.v(LogAutor.AUTHOR_A,TAG,"onCreate " + (++count));

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Logger.v(LogAutor.AUTHOR_A,TAG,"Thread run " + (++count));
//                        Logger.v(LogAutor.AUTHOR_A,TAG,"Thread run " + (++count));
//                    }
//                }).start();

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
//                Logger.v(LogAutor.AUTHOR_A,TAG,str);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.destroy();
    }
}
