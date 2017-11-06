package com.open.loglite.base;

/**
 * Created by long on 2017/11/5.
 */

public class LogMessage {

    public String priority;
    public String tag;
    public String []kvs;

    public LogMessage(String priority, String tag, String[] kvs) {
        this.priority = priority;
        this.tag = tag;
        this.kvs = kvs;
    }
}
