package com.open.loglite;

import android.content.Context;
import android.os.Process;

import com.open.loglite.base.LogConfig;
import com.open.loglite.base.ILog;
import com.open.loglite.loggerimpl.console.ConsLogger;
import com.open.loglite.loggerimpl.file.FileLogger;
import com.open.loglite.loggerimpl.net.NetLogger;

import java.util.ArrayList;

/**
 * Created by long on 2017/9/13.
 */

public final class Logger{

    private ArrayList<ILog> mLoggerList = new ArrayList<>();
    private static Logger mLogger;
    private static LogConfig mLogConfig;

    //------------------------------------------
    public static LogConfig init(Context mContext, String assetFileName, String fileLogPath){
        mLogConfig = LogConfig.parse(mContext,assetFileName);
        if(null != mLogConfig){
            if(mLogConfig.isEnable) {
                mLogger = new Logger();

                if((mLogConfig.common_mode & LogConfig.LOG_MODE_CONSOLE) == LogConfig.LOG_MODE_CONSOLE){
                    mLogger.addLogger(new ConsLogger(mLogConfig.console_log_type));
                }

                if((mLogConfig.common_mode & LogConfig.LOG_MODE_FILE) == LogConfig.LOG_MODE_FILE){
                    mLogger.addLogger(new FileLogger(fileLogPath,mLogConfig.file_name_formater,mLogConfig.file_size,mLogConfig.file_syn));
                }

                if((mLogConfig.common_mode & LogConfig.LOG_MODE_NET) == LogConfig.LOG_MODE_NET){
                    mLogger.addLogger(new NetLogger(mLogConfig.net_tcp));
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
        mLoggerList.add(logger);
    }

    //------------------------------------------
    public static void v(String author , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig|| !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_VERBOSE)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).v(LogConfig.LOG_LEVEL_VERBOSE,tag,kv);
        }
    }

    public static void d(String author , String tag  , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_DEBUG)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).d(LogConfig.LOG_LEVEL_DEBUG,tag,kv);
        }
    }

    public static void i(String author  , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_INFO)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).i(LogConfig.LOG_LEVEL_INFO,tag,kv);
        }
    }

    public static void w(String author , String tag  , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_WARN)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).w(LogConfig.LOG_LEVEL_WARN,tag,kv);
        }
    }

    public static  void e(String author  , String tag , String... kv) {
        if(null == mLogger || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_ERROR)){
            return;
        }

        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                tag = mLogConfig.formatTag(tag,names);
            }
        }

        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).e(LogConfig.LOG_LEVEL_ERROR,tag,kv);
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
                names[4] = ""+Process.myPid();
                names[5] = ""+Process.myTid();
                return true;
            }
        }

        return false;
    }
}
