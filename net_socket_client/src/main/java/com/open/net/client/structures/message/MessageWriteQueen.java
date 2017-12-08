package com.open.net.client.structures.message;

import java.util.LinkedList;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   写队列
 */

public final class MessageWriteQueen {

    private MessageBuffer       mWriteMessageBuffer = new MessageBuffer();//消息缓存
    public  LinkedList<Message> mMessageQueen       = new LinkedList<>();//真正的消息队列

    public Message build(byte[] src , int offset , int length){
        Message msg = mWriteMessageBuffer.build(src,offset,length);
        return msg;
    }

    public void add(Message msg){
        if(null != msg){
            mMessageQueen.add(msg);
        }
    }

    public void remove(Message msg){
        if(null != msg){
            mMessageQueen.remove(msg);
            mWriteMessageBuffer.release(msg);
        }
    }
}
