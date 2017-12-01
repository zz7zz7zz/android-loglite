package com.open.util.log.impl.net;

import com.open.util.log.base.ILog;
import com.open.util.log.net.client.NioClient;
import com.open.util.log.net.data.AbsMessage;
import com.open.util.log.net.data.TcpAddress;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 网络日志
 * Created by long on 2017/9/13.
 */

public final class NetLogger implements ILog {

    @Override
    public void v(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new NetLogMessage(LOG_VERBOSE,tag,trace,kv));
    }

    @Override
    public void d(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new NetLogMessage(LOG_DEBUG,tag,trace,kv));
    }

    @Override
    public void i(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new NetLogMessage(LOG_INFO,tag,trace,kv));
    }

    @Override
    public void w(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new NetLogMessage(LOG_WARN,tag,trace,kv));
    }

    @Override
    public void e(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new NetLogMessage(LOG_ERROR,tag,trace,kv));
    }

    //------------------------------------------------------------
    private NioClient mNioClient ;
    private StringBuilder builder = new StringBuilder(128);

    public NetLogger(TcpAddress[] tcpArray) {
        mNioClient = new NioClient(tcpArray,null);
    }

    //------------------------------------------------------------
    private boolean println(SocketChannel socketChannel, String priority, String tag, String trace, String... kv){
        boolean ret = false;
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
                        ret = log(socketChannel,priority,tag,trace, builder.toString());
                        if(!ret){
                            return ret;
                        }
                        builder.delete(0, builder.length());
                        count = 0;
                    }

                    //2. 判断本次要打印的记录是否也是大于 LOGGER_ENTRY_MAX_LEN_FIX
                    if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                        int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                        for(int j = 1;j< page;j++){
                            ret = log(socketChannel,priority,tag,trace,kv[i].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                            if(!ret){
                                return ret;
                            }
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
                ret = log(socketChannel,priority,tag,trace, builder.toString());
                if(!ret){
                    return ret;
                }
            }
        }else{
            int length = kv[0].length();
            if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                for(int j = 1;j< page;j++){
                    ret = log(socketChannel,priority,tag,trace,kv[0].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                    if(!ret){
                        return ret;
                    }
                }

                ret = log(socketChannel,priority,tag,trace,kv[0].substring((page-1)*LOGGER_ENTRY_MAX_LEN_FIX,length));
                if(!ret){
                    return ret;
                }
            }else{
                ret = log(socketChannel,priority,tag,trace,kv[0]);
                if(!ret){
                    return ret;
                }
            }
        }
        return ret;
    }

    private boolean log(SocketChannel socketChannel,String priority, String tag,String trace, String kv){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String time = sdf.format(new Date());

        StringBuilder sb = new StringBuilder(time.length()
                + (null != priority ? priority.length() + 1 : 0)
                + (null != tag ? tag.length()+ 1 : 0 )
                + (null != trace ? trace.length()+ 1 : 0)
                + (null != kv ? kv.length()+ 1 :0));
        sb.append(time);
        if(null != priority && priority.length()>0){
            sb.append(" ");
            sb.append(priority);
        }
        if(null != tag && tag.length()>0){
            sb.append(" ");
            sb.append(tag);
        }
        if(null != trace && trace.length()>0){
            sb.append(" ");
            sb.append(trace);
        }
        if(null != kv && kv.length()>0){
            sb.append(" ");
            sb.append(kv);
        }
        sb.append(NEW_LINE);
        ByteBuffer buf=ByteBuffer.wrap(sb.toString().getBytes());
        try {
            socketChannel.write(buf);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //------------------------------------------------------------
    public class NetLogMessage extends AbsMessage {

        public String priority;
        public String tag;
        public String trace;
        public String []kvs;

        public NetLogMessage(String priority, String tag, String trace, String[] kvs) {
            this.priority = priority;
            this.tag = tag;
            this.trace = trace;
            this.kvs = kvs;
        }

        @Override
        public byte[] getPacket() {
            return null;
        }

        @Override
        public boolean write(OutputStream outStream) {
            return false;
        }

        @Override
        public boolean write(SocketChannel socketChannel) {
            return println(socketChannel,priority,tag,trace,kvs);
        }
    }

}
