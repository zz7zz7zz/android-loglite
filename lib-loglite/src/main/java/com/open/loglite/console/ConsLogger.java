package com.open.loglite.console;

import android.util.Log;

import com.open.loglite.base.ILog;

/**
 * 控制台日志
 * Created by long on 2017/9/13.
 */

public final class ConsLogger implements ILog {


    public static final int LOGGER_ENTRY_MAX_LEN    =    (4*1024);//The message may have been truncated by the kernel log driver if msg's length is bigger than LOGGER_ENTRY_MAX_LEN.
    public static final int LOGGER_ENTRY_MAX_LEN_FIX= LOGGER_ENTRY_MAX_LEN / 4;

    public static final int LOG_SYSTEM = 1;
    public static final int LOG_LOG    = 2;

    public int log_type = LOG_LOG;

    private void print(int priority,String tag, String... kv){
        String msg;
        if(kv.length>1){
            StringBuilder sb = new StringBuilder(128);
            for (int i = 0; i < kv.length; i++) {
                sb.append(kv[i]);
            }
            msg = sb.toString();
        }else{
            msg = kv[0];
        }

        if(msg.length()>LOGGER_ENTRY_MAX_LEN_FIX){
            int count = msg.length() % LOGGER_ENTRY_MAX_LEN_FIX == 0 ?  msg.length()/LOGGER_ENTRY_MAX_LEN_FIX : (msg.length()/LOGGER_ENTRY_MAX_LEN_FIX + 1);
            char[] logCharArray = new char[LOGGER_ENTRY_MAX_LEN_FIX];
            int i ;
            for(i = 1;i< count;i++){
                msg.getChars((i-1)*LOGGER_ENTRY_MAX_LEN_FIX,i*LOGGER_ENTRY_MAX_LEN_FIX,logCharArray,0);

                String str = new String(logCharArray,0,(i*LOGGER_ENTRY_MAX_LEN_FIX)- (i-1)*LOGGER_ENTRY_MAX_LEN_FIX);
                print(priority,tag,str);
            }

            msg.getChars((i-1)*LOGGER_ENTRY_MAX_LEN_FIX,msg.length(),logCharArray,0);
            String str = new String(logCharArray,0,msg.length()- (i-1)*LOGGER_ENTRY_MAX_LEN_FIX);
            print(priority,tag,str);

        }else{
            print(priority,tag,msg);
        }
    }

    private void print(int priority , String tag , String msg){
        if((log_type & LOG_SYSTEM) == LOG_SYSTEM){
            System.out.println(msg);
        }

        if((log_type & LOG_LOG) == LOG_LOG){
            Log.println(priority,tag,msg);

/*            if(priority == Config.LOG_LEVEL_VERBOSE){
                Log.v(tag,msg);
            }else if(priority == Config.LOG_LEVEL_DEBUG){
                Log.d(tag,msg);
            }else if(priority == Config.LOG_LEVEL_INFO){
                Log.i(tag,msg);
            }else if(priority == Config.LOG_LEVEL_WARN){
                Log.w(tag,msg);
            }else if(priority == Config.LOG_LEVEL_ERROR){
                Log.e(tag,msg);
            }
*/

        }
    }


    @Override
    public void v(int priority, String tag, String... kv) {
        print(priority,tag,kv);
    }

    @Override
    public void d(int priority, String tag, String... kv) {
        print(priority,tag,kv);
    }

    @Override
    public void i(int priority, String tag, String... kv) {
        print(priority,tag,kv);
    }

    @Override
    public void w(int priority, String tag, String... kv) {
        print(priority,tag,kv);
    }

    @Override
    public void e(int priority, String tag, String... kv) {
        print(priority,tag,kv);
    }
}
