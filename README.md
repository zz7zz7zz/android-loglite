# android-loglite

android-loglite是一个轻量级的用于android上的日志打印库，打印的日志类型有：控制台日志/文件日志/网络日志，并且可同时支持其中的一种或多种，简单轻量小巧！

    a.控制台打印/文件打印/网络打印(TCP / UDP)
    b.控制作者,只有需要打印作者的日志才被打印
    c.控制打印level(真正做到 >level ;=level ; <level)

## 一、配置项说明

    #------------配置格式说明start--------
    # 1.模块名以[]开始               如:[ENABLE]
    # 2.#号之后语句的表示注释         如: #这是一条注释
    # 3.数组以[]括起来,元素以逗号分隔  如: key=[v1,v2]
    #------------配置格式说明end----------
    
    
    [Switch]
    enable = true                                           #配置总开关
    
    [Common]
    common_mode  = 15                                       #0x1代表控制台；0x2代表文件；0x4代表Tcp网络；0x8代表Udp
    common_level = 2                                        #取值有>2;<2;=2 ;     另外2:v日志; 3:d日志;  4:I日志;  5:w日志;  6:e日志
    common_author= [A,B]                                    #代表AB两个人才可以打日志，其它人的日志不可见
    common_authorGroup=[A,B,C,D,E]                          #A,B,C,D,E 代表5个人
    common_tag_formater = (__FILE__:__LINE__)
    
                                                            #common_tag_formater说明1: __FILE__文件名  __CLASS__类名 __FUNCTION__方法名       __LINE__文件行数
                                                            #common_tag_formater说明2: 在控制台中如果需要实现点击源文件进行跳转，则可以将          __FILE__和__LINE__进行连接(__FILE__:__LINE__)即可
    
    [Console]
    console_log_type = 2                                    #终端输出类型    SYSTEM = 1; LOGCAT = 2;
    
    [File]
    file_name_formater=yyyy-MM-dd_%d                        #文件名格式化2017-1-1_1.txt
    file_size = 10240000                                    #长度单位是byte ，所以1M的长度是1024*1024
    file_syn  = true                                        #true 表示异步写，false表示同步写
    
    [Net]
    net_tcp=[192.168.123.1:9999,192.168.123.1:9998]         #tcp配置
    net_udp=[192.168.123.1:9999]                            #udp配置
        
## 二、代码接入说明

初始化：
```java
Logger.init(this,"log_config",getDiskCacheDir(this));
```
日志打印：
 ```java
Logger.v("Author", TAG ,"your msg!)");
```

    
## 三、效果展示 
![](https://github.com/zz7zz7zz/android-loglite/blob/master/log_console.png "控制台打印效果")

![](https://github.com/zz7zz7zz/android-loglite/blob/master/log_file.png "文件打印效果") 

![](https://github.com/zz7zz7zz/android-loglite/blob/master/log_tcp.png "网络TCP打印效果")  

![](https://github.com/zz7zz7zz/android-loglite/blob/master/log_udp.png "网络UDP打印效果") 


------------------------------- so easy -------------------------------



