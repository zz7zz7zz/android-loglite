package com.open.loglite.loggerimpl.net;

import com.open.loglite.base.ILog;
import com.open.loglite.base.LogConfig;
import com.open.loglite.base.LogMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 网络日志
 * Created by long on 2017/9/13.
 */

public final class NetLogger implements ILog {

    @Override
    public void v(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new LogMessage(LOG_VERBOSE,tag,trace,kv));
    }

    @Override
    public void d(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new LogMessage(LOG_DEBUG,tag,trace,kv));
    }

    @Override
    public void i(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new LogMessage(LOG_INFO,tag,trace,kv));
    }

    @Override
    public void w(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new LogMessage(LOG_WARN,tag,trace,kv));
    }

    @Override
    public void e(int priority, String tag, String trace, String... kv) {
        mNioClient.sendMessage(new LogMessage(LOG_ERROR,tag,trace,kv));
    }

    //------------------------------------------------------------
    private NioClient mNioClient ;
    private StringBuilder builder = new StringBuilder(128);

    public NetLogger(LogConfig.Tcp[] tcpArray) {
        mNioClient = new NioClient(tcpArray,null);
    }

    //------------------------------------------------------------
    private class NioClient{

        private final LogMessage SIGNAL_RECONNECT = new LogMessage(null,null,null,null);

        private LogConfig.Tcp[] tcpArray;
        private int index = -1;
        private IConnectionReceiveListener mConnectionReceiveListener;

        //无锁队列
        private ConcurrentLinkedQueue<LogMessage> mMessageQueen = new ConcurrentLinkedQueue();
        private Thread mConnectionThread;
        private NioConnection mConnection;

        private INioConnectListener mNioConnectionListener = new INioConnectListener() {
            @Override
            public void onConnectionSuccess() {

            }

            @Override
            public void onConnectionFailed() {
                sendMessage(SIGNAL_RECONNECT);//发送一个空的消息进行SocketConnect操作
            }
        };

        public NioClient(LogConfig.Tcp[] tcpArray, IConnectionReceiveListener mConnectionReceiveListener) {
            this.tcpArray = tcpArray;
            this.mConnectionReceiveListener = mConnectionReceiveListener;
        }

        public void connect() {
            sendMessage(SIGNAL_RECONNECT);
        }

        public void sendMessage(LogMessage msg){
            //1.重连消息，进行重连
            //2.没有连接,需要进行重连
            //3.在连接不成功，并且也不在重连中时，需要进行重连;
            if(SIGNAL_RECONNECT == msg ){
                openConnection();
            }else if(null == mConnection){
                mMessageQueen.add(msg);
                openConnection();
            }else if(!mConnection.isConnected() && !mConnection.isConnecting()){
                mMessageQueen.add(msg);
                openConnection();
            }else{
                mMessageQueen.add(msg);
                if(mConnection.isConnected()){
                    mConnection.selector.wakeup();
                }else{
                    //说明正在重连中
                }
            }
        }

        public synchronized void openConnection(){
            //已经在连接中就不再进行连接
            if(null != mConnection && !mConnection.isClosed()){
                return;
            }

            index++;
            if(index < tcpArray.length && index >= 0){
                closeConnection();
                mConnection = new NioConnection(mMessageQueen,mNioConnectionListener);
                mConnection.init(tcpArray[index].ip,tcpArray[index].port);
                mConnectionThread =new Thread(mConnection);
                mConnectionThread.start();
            }else{
                index = -1;

                //循环连接了一遍还没有连接上，说明网络连接不成功，此时清空消息队列，防止队列堆积
                mMessageQueen.clear();
            }
        }

        public synchronized void closeConnection(){
            try {
                if( null!= mConnectionThread && mConnectionThread.isAlive() ) {
                    mConnectionThread.interrupt();
                }
                mConnectionThread =null;

                if(null != mConnection && !mConnection.isClosed()) {
                    mConnection.close();
                }
                mConnection = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class NioConnection implements Runnable{

        private final String TAG = "NioConnection";

        private final int STATE_CLOSE           =   1<<1;//socket关闭
        private final int STATE_CONNECT_START   =   1<<2;//开始连接server
        private final int STATE_CONNECT_SUCCESS =   1<<3;//连接成功
        private final int STATE_CONNECT_FAILED  =   1<<4;//连接失败

        private Selector selector;
        private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
        private SocketChannel socketChannel;

        private int state= STATE_CLOSE;
        private ConcurrentLinkedQueue<LogMessage> mMessageQueen;
        private INioConnectListener mNioConnectionListener;

        private String ip ="192.168.1.1";
        private int port =9999;

        public NioConnection(ConcurrentLinkedQueue<LogMessage> queen, INioConnectListener mNioConnectionListener) {
            this.mMessageQueen = queen;
            this.mNioConnectionListener = mNioConnectionListener;
        }

        public void init(String ip, int port){
            this.ip = ip;
            this.port = port;
        }

        public boolean isClosed(){
            return state == STATE_CLOSE;
        }

        public void close(){
            if(state != STATE_CLOSE){
                state = STATE_CLOSE;
                if(null!=socketChannel)
                {
                    try {
                        SelectionKey key = socketChannel.keyFor(selector);
                        if(null != key){
                            key.cancel();
                        }
                        selector.close();
                        socketChannel.socket().close();
                        socketChannel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        public boolean isConnected(){
            return state == STATE_CONNECT_SUCCESS;
        }

        public boolean isConnecting(){
            return state == STATE_CONNECT_START;
        }

        @Override
        public void run() {
            try {
                state = STATE_CONNECT_START;
                setConnectionTimeout(10);
                selector= SelectorProvider.provider().openSelector();
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);

                InetSocketAddress address=new InetSocketAddress(ip, port);
                socketChannel.connect(address);
                socketChannel.register(selector, SelectionKey.OP_CONNECT);

                while(state != STATE_CLOSE)
                {
                    selector.select();
                    Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext())
                    {
                        SelectionKey key =  selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid())
                        {
                            continue;
                        }

                        if (key.isConnectable())
                        {
                            finishConnection(key);
                        }
                        else if (key.isReadable())
                        {
                            boolean ret = read(key);
                            if(!ret){
                                throw new Exception("Server Stopping !");
                            }
                        }
                        else if (key.isWritable())
                        {
                            write(key);
                        }
                    }

                    if(!mMessageQueen.isEmpty()) {
                        SelectionKey  key=socketChannel.keyFor(selector);
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                close();
                if(null != mNioConnectionListener){
                    mNioConnectionListener.onConnectionFailed();
                }
            }
        }

        private boolean finishConnection(SelectionKey key) throws IOException
        {
            boolean result;
            SocketChannel socketChannel = (SocketChannel) key.channel();
            result= socketChannel.finishConnect();//没有网络的时候也返回true
            if(result)
            {
                key.interestOps(SelectionKey.OP_READ);
                state=STATE_CONNECT_SUCCESS;
                this.mNioConnectionListener.onConnectionSuccess();
            }
            return result;
        }

        private boolean read(SelectionKey key) throws IOException
        {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            readBuffer.clear();
            int numRead;
            numRead = socketChannel.read(readBuffer);
            if (numRead == -1)
            {
                key.channel().close();
                key.cancel();
                return false;
            }
            return true;
        }

        private void write(SelectionKey key) throws IOException
        {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            while (!mMessageQueen.isEmpty()){
                LogMessage msg = mMessageQueen.poll();
                println(socketChannel,msg.priority,msg.tag,msg.trace,msg.kvs);
            }
            key.interestOps(SelectionKey.OP_READ);
        }

        private void setConnectionTimeout(long timeout){
            new Thread(new NioConnectStateWatcher(timeout)).start();
        }

        private class NioConnectStateWatcher implements Runnable{

            public long timeout;//单位是秒

            public NioConnectStateWatcher(long timeout) {
                this.timeout = timeout;
            }

            @Override
            public void run() {
                long start = System.nanoTime();
                while(true){
                    if(isConnecting()){
                        if((System.nanoTime() - start)/1000000000 > timeout){
                            close();
                            break;
                        }else{
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }else{
                        break;
                    }
                }
            }
        }
    }


    private interface INioConnectListener{

        void onConnectionSuccess();

        void onConnectionFailed();

    }

    public interface IConnectionReceiveListener
    {
        void onConnectionResponse(String txt);
    }

    private void println(SocketChannel socketChannel, String priority, String tag, String trace, String... kv){
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
                        log(socketChannel,priority,tag,trace, builder.toString());
                        builder.delete(0, builder.length());
                        count = 0;
                    }

                    //2. 判断本次要打印的记录是否也是大于 LOGGER_ENTRY_MAX_LEN_FIX
                    if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                        int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                        for(int j = 1;j< page;j++){
                            log(socketChannel,priority,tag,trace,kv[i].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
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
                log(socketChannel,priority,tag,trace, builder.toString());
            }
        }else{
            int length = kv[0].length();
            if(length>LOGGER_ENTRY_MAX_LEN_FIX){
                int page = length % LOGGER_ENTRY_MAX_LEN_FIX == 0 ? length/LOGGER_ENTRY_MAX_LEN_FIX : (length/LOGGER_ENTRY_MAX_LEN_FIX + 1);
                for(int j = 1;j< page;j++){
                    log(socketChannel,priority,tag,trace,kv[0].substring((j-1)*LOGGER_ENTRY_MAX_LEN_FIX,j*LOGGER_ENTRY_MAX_LEN_FIX));
                }

                log(socketChannel,priority,tag,trace,kv[0].substring((page-1)*LOGGER_ENTRY_MAX_LEN_FIX,length));
            }else{
                log(socketChannel,priority,tag,trace,kv[0]);
            }
        }
    }

    private void log(SocketChannel socketChannel,String priority, String tag,String trace, String kv){

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
