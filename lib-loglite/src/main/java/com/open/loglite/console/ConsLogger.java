package com.open.loglite.console;

import android.util.Log;

import com.open.loglite.base.ILog;

/**
 * 控制台日志
 * Created by long on 2017/9/13.
 */

public final class ConsLogger implements ILog {


    @Override
    public void v(String... kv) {
        String msg;
        if(kv.length>1){
            StringBuilder sb = new StringBuilder(128);
            for (int i = 0; i < kv.length; i++) {
                sb.append(kv[i]);
            }
            msg = sb.toString();
        }else{
            msg = kv[0];
        }

        System.out.println(msg);
        Log.v("ConsLogger",msg);
    }

    @Override
    public void d(String... kv) {

    }

    @Override
    public void i(String... kv) {

    }

    @Override
    public void w(String... kv) {

    }

    @Override
    public void e(String... kv) {

    }
}
