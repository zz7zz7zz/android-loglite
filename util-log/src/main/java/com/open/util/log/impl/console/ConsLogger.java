package com.open.util.log.impl.console;

import android.util.Log;

import com.open.util.log.base.ILog;

/**
 * 控制台日志
 * Created by long on 2017/9/13.
 */

public final class ConsLogger implements ILog {

    @Override
    public void v(int priority, String tag, String trace, String... kv) {
        println(priority,tag,trace,kv);
    }

    @Override
    public void d(int priority, String tag, String trace, String... kv) {
        println(priority,tag,trace,kv);
    }

    @Override
    public void i(int priority, String tag, String trace, String... kv) {
        println(priority,tag,trace,kv);
    }

    @Override
    public void w(int priority, String tag, String trace, String... kv) {
        println(priority,tag,trace,kv);
    }

    @Override
    public void e(int priority, String tag, String trace, String... kv) {
        println(priority,tag,trace,kv);
    }

    //------------------------------------------------------------
    private static final int LOG_SYSTEM = 1;
    private static final int LOG_LOGCAT = 2;

    private int console_log_type = LOG_LOGCAT;
    private StringBuilder builder = new StringBuilder(128);

    public ConsLogger(int console_log_type) {
        this.console_log_type = console_log_type;
    }

    private void println(int priority, String tag, String trace, String... kv){
        builder.setLength(0);
        if(kv.length>1){

            int count = 0;
            for (int i = 0; i < kv.length; i++) {
                if(null == kv[i]){
                    continue;
                }

                int length = kv[i].length();
                //一旦加上这个msg 大于 LOGGER_ENTRY_MAX_LEN_FIX ，1.需要马上打印之前的日志，打印后清空; 2.接着重新填入
                if((count + length) > LOGGER_ENTRY_MAX_LEN_FIX){

                    //1. 把上次记录先打印
                    if(builder.length()>0){
                        log(priority,tag,trace, builder.toString());
                        builder.delete(0, builder.length());
                        count = 0;
                    }

                    //2. 判断本次要打印的记录是否也是大于 LOGGER_ENTRY_MAX_LEN_FIX
                    if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                        int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                        for(int j = 1;j< page;j++){
                            log(priority,tag,trace,kv[i].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                        }

                        count += length;
                        builder.append(kv[i].substring((page-1)*LOGGER_ENTRY_MAX_LEN_FIX,length));

                    }else{
                        count += length;
                        builder.append(kv[i]);
                    }

                }else{

                    count += kv[i].length();
                    builder.append(kv[i]);
                }
            }

            if(builder.length()>0){
                log(priority,tag,trace, builder.toString());
            }
        }else{
            int length = kv[0].length();
            if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                for(int j = 1;j< page;j++){
                    log(priority,tag,trace,kv[0].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                }

                log(priority,tag,trace,kv[0].substring((page-1)*LOGGER_ENTRY_MAX_LEN_FIX,length));
            }else{
                log(priority,tag,trace,kv[0]);
            }
        }
    }

    private void log(int priority , String tag , String trace, String msg){
        if((console_log_type & LOG_SYSTEM) == LOG_SYSTEM){
            System.out.println(tag+" "+ trace+ " " + msg);
        }

        if((console_log_type & LOG_LOGCAT) == LOG_LOGCAT){
            Log.println(priority,tag,trace + " " +msg);

/*            if(priority == LogConfig.LOG_LEVEL_VERBOSE){
                Log.v(tag,msg);
            }else if(priority == LogConfig.LOG_LEVEL_DEBUG){
                Log.d(tag,msg);
            }else if(priority == LogConfig.LOG_LEVEL_INFO){
                Log.i(tag,msg);
            }else if(priority == LogConfig.LOG_LEVEL_WARN){
                Log.w(tag,msg);
            }else if(priority == LogConfig.LOG_LEVEL_ERROR){
                Log.e(tag,msg);
            }
*/

        }
    }

}
