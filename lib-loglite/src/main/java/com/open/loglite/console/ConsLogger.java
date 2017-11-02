package com.open.loglite.console;

import android.util.Log;

import com.open.loglite.base.ILog;

/**
 * 控制台日志
 * Created by long on 2017/9/13.
 */

public final class ConsLogger implements ILog {


    public static final int LOGGER_ENTRY_MAX_LEN    =    (4*1000);//The message may have been truncated by the kernel log driver if msg's length is bigger than LOGGER_ENTRY_MAX_LEN.
    public static final int LOGGER_ENTRY_MAX_LEN_FIX= LOGGER_ENTRY_MAX_LEN / 4;

    public static final int LOG_SYSTEM = 1;
    public static final int LOG_LOG    = 2;

    public int log_type = LOG_LOG;

    private void print(int priority,String tag, String... kv){
        if(kv.length>1){
            StringBuilder sb = new StringBuilder(LOGGER_ENTRY_MAX_LEN_FIX);
            int count = 0;
            for (int i = 0; i < kv.length; i++) {
                if(null == kv[i]){
                    continue;
                }

                int length = kv[i].length();
                //一旦加上这个msg 大于 LOGGER_ENTRY_MAX_LEN_FIX ，1.需要马上打印之前的日志，打印后清空; 2.接着重新填入
                if((count + length) > LOGGER_ENTRY_MAX_LEN_FIX){

                    //1. 把上次记录先打印
                    if(sb.length()>0){
                        print(priority,tag,sb.toString());
                        sb.delete(0,sb.length());
                        count = 0;
                    }

                    //2. 判断本次要打印的记录是否也是大于 LOGGER_ENTRY_MAX_LEN_FIX
                    if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                        int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                        for(int j = 1;j< page;j++){
                            print(priority,tag,kv[i].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                        }

                        count += length;
                        sb.append(kv[i].substring((page-1)*LOGGER_ENTRY_MAX_LEN_FIX,length));

                    }else{
                        count += length;
                        sb.append(kv[i]);
                    }

                }else{

                    count += kv[i].length();
                    sb.append(kv[i]);
                }
            }

            if(sb.length()>0){
                print(priority,tag,sb.toString());
            }
        }else{
            int length = kv[0].length();
            if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                for(int j = 1;j< page;j++){
                    print(priority,tag,kv[0].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                }

                print(priority,tag,kv[0].substring((page-1)*LOGGER_ENTRY_MAX_LEN_FIX,length));
            }else{
                print(priority,tag,kv[0]);
            }
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
