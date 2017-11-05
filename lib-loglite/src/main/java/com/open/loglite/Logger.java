package com.open.loglite;

import android.content.Context;

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
    private static Logger mLogger;
    private static Config mLogConfig;

    //------------------------------------------
    public static Config init(Context mContext, String assetFileName,String fileLogPath){
        mLogConfig = Config.parse(mContext,assetFileName);
        if(null != mLogConfig){
            if(mLogConfig.isEnable) {
                mLogger = new Logger();

                if((mLogConfig.logMode & Config.LOG_MODE_CONSOLE) == Config.LOG_MODE_CONSOLE){
                    mLogger.addLogger(new ConsLogger());
                }

                if((mLogConfig.logMode & Config.LOG_MODE_FILE) == Config.LOG_MODE_FILE){
                    mLogger.addLogger(new FileLogger(fileLogPath,mLogConfig.fileNameFormater,mLogConfig.fileSize,mLogConfig.syn));
                }

                if((mLogConfig.logMode & Config.LOG_MODE_NET) == Config.LOG_MODE_NET){
                    mLogger.addLogger(new NetLogger());
                }
            }else{
                destroy();
            }
        } else{
            destroy();
        }

        return mLogConfig;
    }

    public static void destroy(){
        mLogConfig = null;
        mLogger = null;
    }

    //------------------------------------------
    private void addLogger(ILog logger){
        loggerArray.add(logger);
    }

    //------------------------------------------
    public static void v(String author , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig|| !mLogConfig.isPermit(author,Config.LOG_LEVEL_VERBOSE)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[4];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).v(Config.LOG_LEVEL_VERBOSE,tag,kv);
        }
    }

    public static void d(String author , String tag  , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_DEBUG)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[4];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).d(Config.LOG_LEVEL_DEBUG,tag,kv);
        }
    }

    public static void i(String author  , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_INFO)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[4];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).i(Config.LOG_LEVEL_INFO,tag,kv);
        }
    }

    public static void w(String author , String tag  , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_WARN)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[4];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).w(Config.LOG_LEVEL_WARN,tag,kv);
        }
    }

    public static  void e(String author  , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author,Config.LOG_LEVEL_ERROR)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[4];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.loggerArray.size();
        for (int i = 0; i < size; i++) {
            mLogger.loggerArray.get(i).e(Config.LOG_LEVEL_ERROR,tag,kv);
        }
    }

    private static boolean fillTraceNames(String[] names){
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts != null) {
            for (StackTraceElement st:sts) {
                if (st.isNativeMethod()) {
                    continue;
                }

                if (st.getClassName().equals(Thread.class.getName())) {
                    continue;
                }

                if (st.getClassName().equals(Logger.class.getName())) {
                    continue;
                }

                names[0] = st.getFileName();
                names[1] = st.getClassName();
                names[2] = st.getMethodName()+"()";
                names[3] = ""+st.getLineNumber();
                return true;
            }
        }

        return false;
    }
}
