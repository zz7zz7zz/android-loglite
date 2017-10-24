package com.open.loglite;

import android.content.Context;

import com.open.loglite.base.CfgParser;
import com.open.loglite.base.Config;
import com.open.loglite.base.ILog;
import com.open.loglite.console.ConsLogger;
import com.open.loglite.file.FileLogger;
import com.open.loglite.net.NetLogger;

import java.util.ArrayList;

/**
 * Created by long on 2017/9/13.
 */

public final class Logger{

    private ArrayList<ILog> loggerArray = new ArrayList<>();

    private void addLogger(ILog logger){
        loggerArray.add(logger);
    }

    private static Logger mLogger;
    private static Config mLogConfig;

    public static void init(Context mContext, String assetFileName){
        mLogConfig = CfgParser.parse(mContext,assetFileName);
System.out.println(mLogConfig);
        if(mLogConfig.isEnable){
            if(null != mLogConfig){
                mLogger = new Logger();

                if((mLogConfig.logMode & Config.LOG_MODE_CONSOLE) == Config.LOG_MODE_CONSOLE){
                    mLogger.addLogger(new ConsLogger());
                }

                if((mLogConfig.logMode & Config.LOG_MODE_FILE) == Config.LOG_MODE_FILE){
                    mLogger.addLogger(new FileLogger());
                }

                if((mLogConfig.logMode & Config.LOG_MODE_NET) == Config.LOG_MODE_NET){
                    mLogger.addLogger(new NetLogger());
                }
            }
        }else{
            destroy();
        }
    }

    public static void destroy(){
        mLogConfig = null;
        mLogger = null;
    }

    //------------------------------------------
    public static void v(String author , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig|| !mLogConfig.isPermit(author,Config.LOG_LEVEL_VERBOSE)){
            return;
        }

        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).v(kv);
        }
    }

    public static void d(String author , String tag  , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_DEBUG)){
            return;
        }
        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).d(kv);
        }
    }

    public static void i(String author  , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_INFO)){
            return;
        }
        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).i(kv);
        }
    }

    public static void w(String author , String tag  , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_WARN)){
            return;
        }
        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).w(kv);
        }
    }

    public static  void e(String author  , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_ERROR)){
            return;
        }
        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).e(kv);
        }
    }

}
