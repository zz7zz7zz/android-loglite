# android-loglite


    #------------配置格式说明start--------
    # 1.模块名以[]开始               如:[ENABLE]
    # 2.#号之后语句的表示注释         如: #这是一条注释
    # 3.数组以[]括起来,元素以逗号分隔  如: key=[v1,v2]
    #------------配置格式说明end----------

[ENABLE]<br>
enable = true                                           #配置总开关<br>
<br>
[Console]<br>
console_log_type = 2                                    #终端输出类型    SYSTEM = 1; LOGCAT = 2;<br>
<br>
[Common]<br>
common_mode  = 4                                        #0x1代表控制台；0x2代表文件；0x4代表网络<br>
common_level = 2                                        #取值有>2;<2;=2 ;     另外2:v日志; 3:d日志;  4:I日志;  5:w日志;  6:e日志<br>
common_author= [A,B]                                    #代表AB两个人才可以打日志，其它人的日志不可见<br>
common_authorGroup=[A,B,C,D,E]                          #A,B,C,D,E 代表5个人<br>
common_tag_formater =__PID__ __TID__ __FILE__ __CLASS__ __FUNCTION__ __LINE__ __TAG__ # %__FILE__文件名  %__CLASS__类名 %__FUNCTION__方法名 %__LINE__文件行数 __TAG__TAG<br>
<br>
[File]<br>
file_name_formater=yyyy-MM-dd_%d                        #文件名格式化2017-1-1_1.txt<br>
file_size = 10240000                                    #长度单位是byte ，所以1M的长度是1024*1024<br>
file_syn  = true<br>
<br>
[Net]<br>
net_tcp=[192.168.123.1:9999,192.168.123.1:9998]         #tcp配置<br>
net_udp=[192.168.123.1:9999]                            #udp配置<br>
