package com.open.loglite.base;

/**
 * Created by long on 2017/11/5.
 */

public final class LogMessage {

    public String priority;
    public String tag;
    public String trace;
    public String []kvs;

    public LogMessage(String priority, String tag, String trace, String[] kvs) {
        this.priority = priority;
        this.tag = tag;
        this.trace = trace;
        this.kvs = kvs;
    }
}
