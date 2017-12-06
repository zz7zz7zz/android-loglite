package com.open.util.log.base;

/**
 * Created by long on 2017/9/13.
 */

public interface ILog {

    int LOGGER_ENTRY_MAX_LEN    =    (4*1024);//单条log最长为4K
    int LOGGER_ENTRY_MAX_LEN_FIX= LOGGER_ENTRY_MAX_LEN / 4;
    String NEW_LINE = System.getProperty("line.separator");

    String LOG_VERBOSE = "V/";
    String LOG_DEBUG   = "D/";
    String LOG_INFO    = "I/";
    String LOG_WARN    = "W/";
    String LOG_ERROR   = "E/";

    int INDEX_FILENAME      = 0;
    int INDEX_CLASSNAME     = 1;
    int INDEX_METHODNAME    = 2;
    int INDEX_LINENUMBER    = 3;
    int INDEX_PID           = 4;
    int INDEX_TID           = 5;

    void v(int priority , String tag , String trace , String ... kv);

    void d(int priority , String tag , String trace , String ... kv);

    void i(int priority , String tag , String trace , String ... kv);

    void w(int priority , String tag , String trace , String ... kv);

    void e(int priority , String tag , String trace , String ... kv);

}
