package com.open.loglite.base;

/**
 * Created by long on 2017/11/5.
 */

public class LogMessage {

    public String tag;
    public String []kvs;

    public LogMessage(String tag, String ... kvs) {
        this.tag = tag;
        this.kvs = kvs;
    }
}
