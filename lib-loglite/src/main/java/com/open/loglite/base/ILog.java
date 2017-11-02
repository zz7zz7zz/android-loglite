package com.open.loglite.base;

/**
 * Created by Administrator on 2017/9/13.
 */

public interface ILog {

    void v(int priority , String tag , String ... kv);

    void d(int priority , String tag , String ... kv);

    void i(int priority , String tag , String ... kv);

    void w(int priority , String tag , String ... kv);

    void e(int priority , String tag , String ... kv);

}
