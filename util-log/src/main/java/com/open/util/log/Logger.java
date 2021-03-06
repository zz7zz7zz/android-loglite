package com.open.util.log;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.open.util.log.base.ILog;
import com.open.util.log.base.LogConfig;
import com.open.util.log.impl.console.ConsLogger;
import com.open.util.log.impl.file.FileLogger;
import com.open.util.log.impl.net.TcpLogger;
import com.open.util.log.impl.net.UdpLogger;
import com.open.util.log.util.CfgParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by long on 2017/9/13.
 */

public final class Logger{

    private ArrayList<ILog> mLoggerList = new ArrayList<>();
    private static Logger mLogger;
    private static LogConfig mLogConfig;
    private static boolean isInitialized = false;

    //------------------------------------------
    public static LogConfig init(Context mContext, String android_asset_filename, String fileLogPath){
        if(!isInitialized){
            try {
                InputStream mInputStream = mContext.getAssets().open(android_asset_filename);
                mLogConfig = LogConfig.parse(CfgParser.parseToMap(mInputStream));
                if(null != mLogConfig){
                    if(mLogConfig.isEnable) {
                        mLogger = new Logger();

                        if((mLogConfig.common_mode & LogConfig.LOG_MODE_CONSOLE) == LogConfig.LOG_MODE_CONSOLE){
                            mLogger.addLogger(new ConsLogger(mLogConfig.console_log_type));
                        }

                        if((mLogConfig.common_mode & LogConfig.LOG_MODE_FILE) == LogConfig.LOG_MODE_FILE){
                            mLogger.addLogger(new FileLogger(fileLogPath,mLogConfig.file_name_formater,mLogConfig.file_size,mLogConfig.file_syn));
                        }

                        if((mLogConfig.common_mode & LogConfig.LOG_MODE_NET_TCP) == LogConfig.LOG_MODE_NET_TCP){
                            mLogger.addLogger(new TcpLogger(mLogConfig.net_tcp));
                        }

                        if((mLogConfig.common_mode & LogConfig.LOG_MODE_NET_UDP) == LogConfig.LOG_MODE_NET_UDP){
                            mLogger.addLogger(new UdpLogger(mLogConfig.net_udp));
                        }

                        mLogger.start();

                    }else{
                        destroy();
                    }
                } else{
                    destroy();
                }
                isInitialized = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //有可能有网络连接，再次初始化一次
            if(null != mLogger){
                mLogger.start();
            }
        }
        return mLogConfig;
    }

    public static LogConfig init(String config_file_path, String fileLogPath){
        if(!isInitialized){
            mLogConfig = LogConfig.parse(CfgParser.parseToMap(config_file_path));
            if(null != mLogConfig){
                if(mLogConfig.isEnable) {
                    mLogger = new Logger();

                    if((mLogConfig.common_mode & LogConfig.LOG_MODE_CONSOLE) == LogConfig.LOG_MODE_CONSOLE){
                        mLogger.addLogger(new ConsLogger(mLogConfig.console_log_type));
                    }

                    if((mLogConfig.common_mode & LogConfig.LOG_MODE_FILE) == LogConfig.LOG_MODE_FILE){
                        mLogger.addLogger(new FileLogger(fileLogPath,mLogConfig.file_name_formater,mLogConfig.file_size,mLogConfig.file_syn));
                    }

                    if((mLogConfig.common_mode & LogConfig.LOG_MODE_NET_TCP) == LogConfig.LOG_MODE_NET_TCP){
                        mLogger.addLogger(new TcpLogger(mLogConfig.net_tcp));
                    }

                    if((mLogConfig.common_mode & LogConfig.LOG_MODE_NET_UDP) == LogConfig.LOG_MODE_NET_UDP){
                        mLogger.addLogger(new UdpLogger(mLogConfig.net_udp));
                    }

                    mLogger.start();

                }else{
                    destroy();
                }
            } else{
                destroy();
            }
            isInitialized = true;
        }else{
            //有可能有网络连接，再次初始化一次
            if(null != mLogger){
                mLogger.start();
            }
        }
        return mLogConfig;
    }

    public static LogConfig updateConfig(String config_file_path, String fileLogPath){
        destroy();
        return init(config_file_path,fileLogPath);
    }

    public static void destroy(){
        if(null != mLogger){
            mLogger.stop();
        }
        mLogConfig = null;
        mLogger = null;
        isInitialized = false;
    }

    //------------------------------------------
    private void addLogger(ILog logger){
        mLoggerList.add(logger);
    }

    private void start(){
        for (ILog logger : mLogger.mLoggerList){
            logger.start();
        }
    }

    private void stop(){
        for (ILog logger : mLogger.mLoggerList){
            logger.stop();
        }
        mLogger = null;
    }

    //------------------------------------------
    public static void v(String author , String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig ||!mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_VERBOSE)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).v(LogConfig.LOG_LEVEL_VERBOSE,tag,trace,logKv);
        }
    }

    public static void d(String author , String tag  , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_DEBUG)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).d(LogConfig.LOG_LEVEL_DEBUG,tag,trace,logKv);
        }
    }

    public static void i(String author  , String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_INFO)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).i(LogConfig.LOG_LEVEL_INFO,tag,trace,logKv);
        }
    }

    public static void w(String author , String tag  , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_WARN)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).w(LogConfig.LOG_LEVEL_WARN,tag,trace,logKv);
        }
    }

    public static  void e(String author  , String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_ERROR)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).e(LogConfig.LOG_LEVEL_ERROR,tag,trace,logKv);
        }
    }

    //------------------------------------------
    public static void v(String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig ||!mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_VERBOSE)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).v(LogConfig.LOG_LEVEL_VERBOSE,"",trace,logKv);
        }
    }

    public static void d(String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_DEBUG)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).d(LogConfig.LOG_LEVEL_DEBUG,"",trace,logKv);
        }
    }

    public static void i(String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_INFO)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).i(LogConfig.LOG_LEVEL_INFO,"",trace,logKv);
        }
    }

    public static void w(String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_WARN)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).w(LogConfig.LOG_LEVEL_WARN,"",trace,logKv);
        }
    }

    public static  void e(String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_ERROR)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).e(LogConfig.LOG_LEVEL_ERROR,"",trace,logKv);
        }
    }

    //--------------------带Tag日志----------------------
    public static void vt(String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig ||!mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_VERBOSE)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).v(LogConfig.LOG_LEVEL_VERBOSE,tag,trace,logKv);
        }
    }

    public static void dt(String tag  , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_DEBUG)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).d(LogConfig.LOG_LEVEL_DEBUG,tag,trace,logKv);
        }
    }

    public static void it(String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_INFO)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).i(LogConfig.LOG_LEVEL_INFO,tag,trace,logKv);
        }
    }

    public static void wt(String tag  , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_WARN)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).w(LogConfig.LOG_LEVEL_WARN,tag,trace,logKv);
        }
    }

    public static  void et(String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermitLevel(LogConfig.LOG_LEVEL_ERROR)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).e(LogConfig.LOG_LEVEL_ERROR,tag,trace,logKv);
        }
    }

    //--------------------带Author/Tag日志----------------------
    public static void vat(String author , String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig ||!mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_VERBOSE)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).v(LogConfig.LOG_LEVEL_VERBOSE,tag,trace,logKv);
        }
    }

    public static void dat(String author , String tag  , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_DEBUG)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).d(LogConfig.LOG_LEVEL_DEBUG,tag,trace,logKv);
        }
    }

    public static void iat(String author  , String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_INFO)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).i(LogConfig.LOG_LEVEL_INFO,tag,trace,logKv);
        }
    }

    public static void wat(String author , String tag  , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_WARN)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).w(LogConfig.LOG_LEVEL_WARN,tag,trace,logKv);
        }
    }

    public static  void eat(String author  , String tag , String... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_ERROR)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = kv;
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).e(LogConfig.LOG_LEVEL_ERROR,tag,trace,logKv);
        }
    }

    //------------------------------------------
    public static void vat(String author , String tag , Object... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig ||!mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_VERBOSE)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = transformMessage(kv);
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).v(LogConfig.LOG_LEVEL_VERBOSE,tag,trace,logKv);
        }
    }

    public static void dat(String author , String tag  , Object... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_DEBUG)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = transformMessage(kv);
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).d(LogConfig.LOG_LEVEL_DEBUG,tag,trace,logKv);
        }
    }

    public static void iat(String author  , String tag , Object... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_INFO)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = transformMessage(kv);
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).i(LogConfig.LOG_LEVEL_INFO,tag,trace,logKv);
        }
    }

    public static void wat(String author , String tag  , Object... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_WARN)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = transformMessage(kv);
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).w(LogConfig.LOG_LEVEL_WARN,tag,trace,logKv);
        }
    }

    public static  void eat(String author  , String tag , Object... kv) {
        if(null == mLogger || kv.length == 0 || null == mLogConfig || !mLogConfig.isPermit(author, LogConfig.LOG_LEVEL_ERROR)){
            return;
        }

        String trace = null;
        if(mLogConfig.isCanFormatTag()){
            String [] names = new String[6];
            if(fillTraceNames(names)){
                trace = mLogConfig.getTraceInfo(names);
            }
        }

        String[] logKv = transformMessage(kv);
        int size = mLogger.mLoggerList.size();
        for (int i = 0; i < size; i++) {
            mLogger.mLoggerList.get(i).e(LogConfig.LOG_LEVEL_ERROR,tag,trace,logKv);
        }
    }

    //------------------------------------------
    private static Set<String> filterTraceElements = new HashSet<String>();
    static{
        filterTraceElements.add(Thread.class.getName());
        filterTraceElements.add(Logger.class.getName());
    }
    public static void addFilterTraceElement(String className){
        filterTraceElements.add(className);
    }

    private static boolean fillTraceNames(String[] names){
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts != null) {
            for (StackTraceElement st:sts) {
                if (st.isNativeMethod()) {
                    continue;
                }

                if(filterTraceElements.contains(st.getClassName())){
                    continue;
                }

                names[ILog.INDEX_FILENAME]   = st.getFileName();
                names[ILog.INDEX_CLASSNAME]  = st.getClassName();
                names[ILog.INDEX_METHODNAME] = st.getMethodName()+"()";
                names[ILog.INDEX_LINENUMBER] = ""+st.getLineNumber();
                names[ILog.INDEX_PID]        = ""+Process.myPid();
                names[ILog.INDEX_TID]        = ""+Process.myTid();
                return true;
            }
        }

        return false;
    }

    //-------------------------------------------------------------------
    private static final int JSON_INDENT = 2;
    private static String[] transformMessage(Object[] input){
        String[] ret = new String[input.length];
        for (int i = 0 ;i < input.length; i++){
            ret[i] = transformMessage(input[i]);
        }
        return ret;
    }

    private static String transformMessage(Object input){
        try{
            if(input instanceof String){
                return (String) input;
            }else if(input instanceof JSONObject){
                JSONObject jsonObject = (JSONObject) input;
                return jsonObject.toString(JSON_INDENT);
            }else if(input instanceof JSONArray){
                JSONArray jsonArray = (JSONArray) input;
                return jsonArray.toString(JSON_INDENT);
            }else if (input instanceof Throwable) {
                return Log.getStackTraceString((Throwable) input);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null != input ? input.toString() : "Null";
    }
}
