




aku-trace
1 介绍
        根据tid快速模拟测试环境或者线上环境的异常场景并在本地debugger,实现bug的快速重现，简化修复bug的时间精力投入
       
        暂时不建议接入正式环境 高频率的日志io可能造成一定的负荷,暂时只考虑测试环境运行.
2 接入
 2.1项目引入pom文件
<dependency>
 <groupId>com.akulaku.trace</groupId>
 <artifactId>trace-report</artifactId>
 <version>1.0.2-SNAPSHOT</version>
</dependency>
<dependency>
 <groupId>com.akulaku.trace</groupId>
 <artifactId>trace-parse</artifactId>
 <version>1.0.2-SNAPSHOT</version>
 <scope>test</scope>
</dependency>




2.2 自己项目apollo新增配置信息
测试环境
#trace 相关配置
trace:
  enable: true #是否启用
  report: http # log上报 mq上报 http上报


log配置

注意 如果report使用了http上报 并且项目并没有接入skywalking的情况下日志的打印格式加上这个; 
[traceTid=%X{traceTid}] 这里生成了个虚拟的tid


如果要使用ai检测功能 需要额外在trace-service服务 配置以下信息 ,此举是让trace-service 获取异常服务的源代码和找到启动类
    #项目名称 
    "order-service": 
        #子项目最外层路径 没有就填空或者去掉此项配置
        serviceName: order-service-impl 
        #gitlab的地址
        gitlabUrl: https://git.xxxxxxx.com/aku-ec-service/order-service.git 
        #main方法全路径名称
        mainClass: com.akulaku.service.orderservice.OrderServiceMain


3 使用


暂时只支持springboot的 configuration
1.选中可以运行的springboot 类型的 configuration,然后点击GPT图标

2点击图标弹出弹框,录入tid, day指的是哪天的日志,默认为0(今天) 如果是昨天的日志 改成-1,以此类推,

3然后点击ok,程序根据tid,appName,day 搜索测试环境的日志和上报信息,并生成执行类

4,步骤3运行完成后,会自动弹出trace面板
功能1 本地重现




功能2  ai检测

点击提交,然后等, 如果项目是第一次,需要很长时间
 
console-biz-order    2024-05-30 07:56:28.597->|2024-05-30 08:03:35.596 第一次运行将近7分钟,
实现原理:下载console-biz-order代码, 切换test分支,编译代码,生成测试类,编译测试类,debugger运行应用,等到程序执行到代码行|"throw new ConsoleBizException(response.getCode(), response.getMessage());"的时候断点拦截住,在打印出错误堆栈,代码信息,和上下文中的成员变量当前的值,交由gpt分析原因


--------------------------------------------------------------------------------------------------------------------------------
6.26新增功能
新增两种不同的运行方式
+sleep: 主线程挂起,   方便多线程场景, 代码还没debugger住,主线程就挂掉了,   需要手动stop   
仅入参:仅仅mock入口的入参, 过程的对外接口和数据库调用mock全部清除; 偶尔有用, 适用于代码改完后的的整个场景的回顾










