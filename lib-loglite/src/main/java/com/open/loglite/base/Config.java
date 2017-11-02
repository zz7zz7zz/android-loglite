package com.open.loglite.base;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 配置规则
 * Created by Administrator on 2017/9/13.
 */

public class Config {

    public static final int LOG_MODE_CONSOLE = 0x1;
    public static final int LOG_MODE_FILE    = 0x2;
    public static final int LOG_MODE_NET     = 0x4;

    public static final int LOG_LEVEL_VERBOSE = 2;
    public static final int LOG_LEVEL_DEBUG   = 3;
    public static final int LOG_LEVEL_INFO    = 4;
    public static final int LOG_LEVEL_WARN    = 5;
    public static final int LOG_LEVEL_ERROR   = 6;

    public static final int COMPARE_TYPE_GT = 1;//大于
    public static final int COMPARE_TYPE_EQ = 2;//等于
    public static final int COMPARE_TYPE_LT = 3;//小于

    public static final String __FILE__     = "__FILE__";
    public static final String __CLASS__    = "__CLASS__";
    public static final String __FUNCTION__ = "__FUNCTION__";
    public static final String __LINE__     = "__LINE__";
    public static final String __TAG__      = "__TAG__";

    public static final int FLAG__FILE__    = 0X0001;
    public static final int FLAG__CLASS__   = 0X0002;
    public static final int FLAG__FUNCTION__= 0X0004;
    public static final int FLAG__LINE__    = 0X0008;
    public static final int FLAG__TAG__     = 0X0010;

    /*
    logConfig.txt 样式如下：
    #------------配置格式说明start--------
    # 1.模块名以[]开始               如:[ENABLE]
    # 2.#号之后语句的表示注释         如: #这是一条注释
    # 3.数组以[]括起来,元素以逗号分隔  如: key=[v1,v2]
    #------------配置格式说明end----------


    [ENABLE]
    enable = true                                   #配置总开关

    [Common]
    logMode  = 1                                    #0x1代表控制台；0x2代表文件；0x4代表网络
    logLevel = 2                                    #取值有>2;<2;=2 ;     另外2:v日志; 3:d日志;  4:I日志;  5:w日志;  6:e日志
    logAuthor= A|B                                  #代表AB两个人,默认值为全部
    authorGroup=[A,B,C,D,E]                         #A,B,C,D,E 代表5个人
    logFormater = "__FILE__  __CLASS__ __FUNCTION__ __LINE__ __TAG__ " # %__FILE__文件名  %__CLASS__类名 %__FUNCTION__方法名 %__LINE__文件行数 __TAG__TAG

    [File]
    fileNameFormater="yyyy-MM-dd"                   #文件名格式化
    fileSize = 1024                                 #长度单位是byte ，所以1M的长度是1024*1024

    [Net]
    tcp=[127.0.0.1:9999,127.0.0.1:9998]             #tcp配置
    udp=127.0.0.1:9999                              #udp配置

     */

    //-----------commonConfig----------
    public boolean isEnable = false; //是否开启了日志
    public int logMode      = 1;    //打印日志的方式
    public int logLevel     = 0;   //哪些等级可以输出
    public int compareType = COMPARE_TYPE_EQ;
    public String[] logAuthor   ;    //哪些作者的日志可以输出,支持64个作者
    public HashMap<String,Boolean> logAuthorMap;
    public String[] authorGroup;//有哪些作者
    public String logFormater;//日志需要格式化成什么样的
    public int formatFlag ;

    //-----------fileConfig----------
    public String fileNameFormater;//命名规则
    public long fileSize;//每一个文件大小

    //-----------netConfig----------
    public Tcp[] tcpArray;
    public Udp[] udpArray;

    public static class Tcp{
        public String  ip;
        public int     port;

        @Override
        public String toString() {
            return "Tcp{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    public static class Udp{
        public String  ip;
        public int     port;

        @Override
        public String toString() {
            return "Udp{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    public static Config parse(Context mContext , String assetFileName) {
        HashMap<String,Object> ret = CfgParser.parseToMap(mContext,assetFileName);
        Config mLogConfig = new Config();
        mLogConfig.init(ret);
        return mLogConfig;
    }

    private void init(HashMap map){
        if(null != map){
            isEnable        = CfgParser.getBoolean(map,"ENABLE","enable");

            logMode         = CfgParser.getInt(map,"Common","logMode");
            String _logLevel        = CfgParser.getString(map,"Common","logLevel");
            if(_logLevel.startsWith(">")){
                compareType = COMPARE_TYPE_GT;
                logLevel = Integer.valueOf(_logLevel.substring(1));
            }else if(_logLevel.startsWith("<")){
                compareType = COMPARE_TYPE_LT;
                logLevel = Integer.valueOf(_logLevel.substring(1));
            }else if(_logLevel.startsWith("=")){
                compareType = COMPARE_TYPE_EQ;
                logLevel = Integer.valueOf(_logLevel.substring(1));
            }else {
                compareType = COMPARE_TYPE_EQ;
                logLevel = Integer.valueOf(_logLevel.substring(0));
            }
            logFormater     = CfgParser.getString(map,"Common","logFormater");
            if(logFormater.contains(__FILE__)){
                formatFlag |= FLAG__FILE__;
            }
            if(logFormater.contains(__CLASS__)){
                formatFlag |= FLAG__CLASS__;
            }
            if(logFormater.contains(__FUNCTION__)){
                formatFlag |= FLAG__FUNCTION__;
            }
            if(logFormater.contains(__LINE__)){
                formatFlag |= FLAG__LINE__;
            }
            if(logFormater.contains(__TAG__)){
                formatFlag |= FLAG__TAG__;
            }

            logAuthor       = CfgParser.getStringArray(map,"Common","logAuthor");
            authorGroup     = CfgParser.getStringArray(map,"Common","authorGroup");
            if(null != logAuthor && logAuthor.length>0){
                logAuthorMap = new HashMap<>(logAuthor.length);
                for (int i = 0; i < logAuthor.length; i++) {
                    logAuthorMap.put(logAuthor[i],true);
                }
            }

            fileNameFormater = CfgParser.getString(map,"File","fileNameFormater");
            fileSize         = CfgParser.getLong(map,"File","fileSize");

            String val[]     = CfgParser.getStringArray(map,"Net","tcp");
            if(null != val){
                tcpArray = new Tcp[val.length];
                for (int i = 0; i < val.length; i++) {
                    String[] v = val[i].split(":");
                    if(v.length>1){
                        tcpArray[i]  = new Tcp();
                        tcpArray[i].ip = v[0];
                        tcpArray[i].port = Integer.valueOf(v[1]);
                    }
                }
            }
            val              = CfgParser.getStringArray(map,"Net","udp");
            if(null != val){
                udpArray = new Udp[val.length];
                for (int i = 0; i < val.length; i++) {
                    String[] v = val[i].split(":");
                    if(v.length>1){
                        udpArray[i] = new Udp();
                        udpArray[i].ip = v[0];
                        udpArray[i].port = Integer.valueOf(v[1]);
                    }
                }
            }
        }
    }

    public boolean isPermit( String author, int level){
        return isEnable && isPermitAuthor(author) && isPermitLevel(level);
    }

    public boolean isPermitAuthor(String author){
        return null != logAuthorMap ? logAuthorMap.get(author) : false;
    }

    public boolean isPermitLevel(int level){
        if(compareType == COMPARE_TYPE_GT){
            return level > logLevel;
        }else if(compareType == COMPARE_TYPE_LT){
            return level < logLevel;
        }else  if(compareType == COMPARE_TYPE_EQ){
            return level == logLevel;
        }
        return level == logLevel;
    }

    public boolean isCanFormatTag(){
        return formatFlag > 0;
    }

    public String formatTag(final String tag , String [] names){
        String ret = logFormater;
        if((formatFlag & FLAG__FILE__) == FLAG__FILE__){
            ret = ret.replaceFirst(__FILE__,names[0]);
        }

        if((formatFlag & FLAG__CLASS__) == FLAG__CLASS__){
            ret = ret.replaceFirst(__CLASS__,names[1]);
        }

        if((formatFlag & FLAG__FUNCTION__) == FLAG__FUNCTION__){
            ret = ret.replaceFirst(__FUNCTION__,names[2]);
        }

        if((formatFlag & FLAG__LINE__) == FLAG__LINE__){
            ret = ret.replaceFirst(__LINE__,names[3]);
        }

        if((formatFlag & FLAG__TAG__) == FLAG__TAG__){
            ret = ret.replaceFirst(__TAG__,tag);
        }

        return ret;
    }

    @Override
    public String toString() {
        return "Config{" +
                "isEnable=" + isEnable +
                ", logMode=" + logMode +
                ", logLevel=" + logLevel +
                ", logAuthor=" + Arrays.toString(logAuthor) +
                ", authorGroup=" + Arrays.toString(authorGroup) +
                ", logFormater='" + logFormater + '\'' +
                ", fileNameFormater='" + fileNameFormater + '\'' +
                ", fileSize=" + fileSize +
                ", tcpArray=" + Arrays.toString(tcpArray) +
                ", udpArray=" + Arrays.toString(udpArray) +
                '}';
    }
}
