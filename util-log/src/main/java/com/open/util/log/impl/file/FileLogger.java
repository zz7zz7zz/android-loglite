package com.open.util.log.impl.file;

import com.open.util.log.base.ILog;
import com.open.util.log.base.LogMessage;

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
    public void v(int priority, String tag, String trace, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_VERBOSE,tag,trace,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        println(LOG_VERBOSE,msg.tag,msg.trace,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            println(LOG_VERBOSE,tag,trace,kv);
            closeFile();
        }
    }

    @Override
    public void d(int priority, String tag, String trace, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_DEBUG,tag,trace,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        println(LOG_DEBUG,msg.tag,msg.trace,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            println(LOG_DEBUG,tag,trace,kv);
            closeFile();
        }
    }

    @Override
    public void i(int priority, String tag, String trace, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_INFO,tag,trace,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        println(LOG_INFO,msg.tag,msg.trace,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            println(LOG_INFO,tag,trace,kv);
            closeFile();
        }
    }

    @Override
    public void w(int priority, String tag, String trace, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_WARN,tag,trace,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        println(LOG_WARN,msg.tag,msg.trace,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            println(LOG_WARN,tag,trace,kv);
            closeFile();
        }
    }

    @Override
    public void e(int priority, String tag, String trace, String... kv) {
        if(syn){
            mMessageQueen.add(new LogMessage(LOG_ERROR,tag,trace,kv));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(mMessageQueen.isEmpty()){
                        return;
                    }

                    openFile();
                    while (!mMessageQueen.isEmpty()){
                        LogMessage msg = mMessageQueen.poll();
                        println(LOG_ERROR,msg.tag,msg.trace,msg.kvs);
                    }
                    closeFile();
                }
            });
        }else{
            openFile();
            println(LOG_ERROR,tag,trace,kv);
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
    private StringBuilder builder = new StringBuilder(128);
    //------------------------------------------------------------

    public FileLogger(String logPath , String fileNameFormater, long fileSize , boolean isSyn) {
        this.fileSavePath = logPath;
        this.fileNameFormater = fileNameFormater;
        this.fileSize = fileSize;
        this.syn = isSyn;
        initFile();
    }

    private void println(String priority, String tag, String trace, String... kv){
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

    private void log(String priority, String tag, String trace, String kv){

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

                if(null != priority && priority.length()>0){
                    fw.write(" ");
                    fw.write(priority);
                }

                if(null != tag && tag.length()>0){
                    fw.write(" ");
                    fw.write(tag);
                }

                if(null != trace && trace.length()>0){
                    fw.write(" ");
                    fw.write(trace);
                }

                if(null != kv && kv.length()>0){
                    fw.write(" ");
                    fw.write(kv);
                }

                fw.write(NEW_LINE);
                fw.flush();
                writtenSize += (time.length()
                        + (null != priority ? priority.length() + 1 : 0)
                        + (null != tag ? tag.length()+ 1 : 0 )
                        + (null != trace ? trace.length()+ 1 : 0)
                        + (null != kv ? kv.length()+ 1 :0));
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
