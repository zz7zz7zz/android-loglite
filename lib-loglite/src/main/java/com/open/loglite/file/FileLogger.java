package com.open.loglite.file;

import com.open.loglite.base.ILog;
import com.open.loglite.base.LogMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 文件日志
 * Created by long on 2017/9/13.
 */

public final class FileLogger implements ILog {

    @Override
    public void v(int priority, String tag, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_VERBOSE,tag,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        write(LOG_VERBOSE,msg.tag,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            write(LOG_VERBOSE,tag,kv);
            closeFile();
        }
    }

    @Override
    public void d(int priority, String tag, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_DEBUG,tag,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        write(LOG_DEBUG,msg.tag,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            write(LOG_DEBUG,tag,kv);
            closeFile();
        }
    }

    @Override
    public void i(int priority, String tag, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_INFO,tag,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        write(LOG_INFO,msg.tag,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            write(LOG_INFO,tag,kv);
            closeFile();
        }
    }

    @Override
    public void w(int priority, String tag, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_WARN,tag,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        write(LOG_WARN,msg.tag,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            write(LOG_WARN,tag,kv);
            closeFile();
        }
    }

    @Override
    public void e(int priority, String tag, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_ERROR,tag,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        write(LOG_ERROR,msg.tag,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            write(LOG_ERROR,tag,kv);
            closeFile();
        }
    }

    //------------------------------------------------------------
    private final String    fileSavePath;
    private final String    fileNameFormater;//命名规则
    private final long      fileSize;//每一个文件大小
    private final boolean   syn;//是否是异步写文件

    private FileWriter fw = null;
    private int     fileNameIndex = 0;
    private long    writtenSize = 0;
    private String  writtenFileName;
    private ConcurrentLinkedQueue<LogMessage> mMessageQueen = new ConcurrentLinkedQueue();
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(0,1,60L, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

    //------------------------------------------------------------

    public FileLogger(String logPath , String fileNameFormater, long fileSize , boolean isSyn) {
        this.fileSavePath = logPath;
        this.fileNameFormater = fileNameFormater;
        this.fileSize = fileSize;
        this.syn = isSyn;
        initFile();
    }

    private void write(String priority, String tag, String... kv){
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
    }

    private void write(String priority, String tag, String kv){

        if((writtenSize + tag.length() +  kv.length())>= fileSize){//当写入的文件长度大于等于一个文件的大小时，应该重新创建一个新的文件进行写入
            closeFile();
            initFile();
            openFile();
        }

        try {
            if(null != fw){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                String time = sdf.format(new Date());
                fw.write(time);
                fw.write(" ");
                fw.write(priority);
                fw.write(" ");
                fw.write(tag);
                fw.write(" ");
                fw.write(kv);
                fw.write(NEW_LINE);
                writtenSize += (time.length() + 1 +priority.length()+ 1+ tag.length() + 1 +  kv.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean initFile(){
        writtenFileName = null;
        writtenSize = 0;

        while(true){

            ++fileNameIndex;

            String pattern = String.format(fileNameFormater,fileNameIndex);
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            writtenFileName = fileSavePath + sdf.format(new Date())+".txt";
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
        if(null == writtenFileName || null != fw){
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

}
