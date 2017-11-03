package com.open.loglite.file;

import com.open.loglite.base.ILog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件日志
 * Created by long on 2017/9/13.
 */

public final class FileLogger implements ILog {

    public static final String LOG_VERBOSE = "V/";
    public static final String LOG_DEBUG   = "D/";
    public static final String LOG_INFO    = "I/";
    public static final String LOG_WARN    = "W/";
    public static final String LOG_ERROR   = "E/";

    @Override
    public void v(int priority, String tag, String... kv) {
        print(LOG_VERBOSE,tag,kv);
    }

    @Override
    public void d(int priority, String tag, String... kv) {
        print(LOG_DEBUG,tag,kv);
    }

    @Override
    public void i(int priority, String tag, String... kv) {
        print(LOG_INFO,tag,kv);
    }

    @Override
    public void w(int priority, String tag, String... kv) {
        print(LOG_WARN,tag,kv);
    }

    @Override
    public void e(int priority, String tag, String... kv) {
        print(LOG_ERROR,tag,kv);
    }

    //------------------------------------------------------------
    public static final int LOGGER_ENTRY_MAX_LEN    =    (4*1024);
    public static final int LOGGER_ENTRY_MAX_LEN_FIX= LOGGER_ENTRY_MAX_LEN / 4;
    public static final String NEW_LINE = System.getProperty("line.separator");
    private FileWriter fw = null;
    public final String logPath;
    public final String fileNameFormater;//命名规则
    public final long fileSize;//每一个文件大小
    public int fileNameIndex = 0;
    public long writtenedSize = 0;
    private String writtenFileName;

    public FileLogger(String logPath , String fileNameFormater, long fileSize) {
        this.logPath = logPath;
        this.fileNameFormater = fileNameFormater;
        this.fileSize = fileSize;
        initFile();
    }

    private void print(String priority, String tag, String... kv){

        openFile();

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
                        write(priority,tag,sb.toString());
                        sb.delete(0,sb.length());
                        count = 0;
                    }

                    //2. 判断本次要打印的记录是否也是大于 LOGGER_ENTRY_MAX_LEN_FIX
                    if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                        int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                        for(int j = 1;j< page;j++){
                            write(priority,tag,kv[i].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
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
                write(priority,tag,sb.toString());
            }
        }else{
            int length = kv[0].length();
            if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                for(int j = 1;j< page;j++){
                    write(priority,tag,kv[0].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                }

                write(priority,tag,kv[0].substring((page-1)*LOGGER_ENTRY_MAX_LEN_FIX,length));
            }else{
                write(priority,tag,kv[0]);
            }
        }

        closeFile();
    }

    private void write(String priority, String tag, String kv){

        if((writtenedSize + tag.length() +  kv.length())>= fileSize){//当写入的文件长度大于等于一个文件的大小时，应该重新创建一个新的文件进行写入
            closeFile();
            initFile();
            openFile();
        }

        try {
            if(null != fw){
                fw.write(priority);
                fw.write(" ");
                fw.write(tag);
                fw.write(" ");
                fw.write(kv);
                fw.write(NEW_LINE);
                writtenedSize += (priority.length()+ 1+ tag.length() + 1 +  kv.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean initFile(){
        writtenFileName = null;
        writtenedSize = 0;

        while(true){

            ++fileNameIndex;

            String pattern = String.format(fileNameFormater,fileNameIndex);
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            writtenFileName = logPath + sdf.format(new Date())+".txt";
            File file = new File(writtenFileName);

            if(file.exists()) {
                continue;
            }

            if(!file.getParentFile().exists()) {
                if(!file.getParentFile().mkdirs()) {
                    return false;
                }
            }

            try {
                return file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
    }

    private void openFile(){
        if(null == writtenFileName){
            return;
        }
        try {
            fw = new FileWriter(writtenFileName,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeFile(){
        try {
            if(null !=fw){
                fw.close();
                fw = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------
}
