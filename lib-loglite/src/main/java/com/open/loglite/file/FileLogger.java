package com.open.loglite.file;

import com.open.loglite.base.ILog;

/**
 * 文件日志
 * Created by long on 2017/9/13.
 */

public final class FileLogger implements ILog {

    @Override
    public void v(int priority, String tag, String... kv) {
        print(tag,kv);
    }

    @Override
    public void d(int priority, String tag, String... kv) {
        print(tag,kv);
    }

    @Override
    public void i(int priority, String tag, String... kv) {
        print(tag,kv);
    }

    @Override
    public void w(int priority, String tag, String... kv) {
        print(tag,kv);
    }

    @Override
    public void e(int priority, String tag, String... kv) {
        print(tag,kv);
    }

    //------------------------------------------------------------
    private void print(String tag, String... kv){

    }

    private void print(int priority , String tag , String msg){

    }

    //------------------------------------------------------------
}
