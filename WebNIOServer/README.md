# 自制HTTP服务器（包括HTTP服务器和Servlet容器）
## 具备的功能(均为简化版的实现)：

- HTTP Protocol
- Servlet
- ServletContext
- Request
- Response
- Dispatcher
- Static Resources & File Download
- Error Notification
- Get & Post & Put & Delete
- web.xml parse
- Forward
- Redirect
- Simple TemplateEngine
- session
- cookie

## 使用技术

基于Java BIO、多线程、Socket网络编程、XML解析、log4j/slf4j日志
只引入了junit,lombok(简化POJO开发),slf4j,log4j,dom4j(解析xml),mime-util(用于判断文件类型)依赖，与web相关内容全部自己完成。

## 打包
- 必须使用maven的assembly插件，它可以把依赖的jar包打进来并且解压
- 需要指定resources/webapp等，把除了源码之外的资源文件包含进来
- class.getResource方法不推荐使用，因为在jar包中的文件路径是有空格的，但是getResource方法得到的是URL，是没有空格的。如果一定要在jar包中使用getResource，那么必须将URL中的文件路径中的%20替换为空格`getClass().getResource("/a.txt").getPath().replaceAll("%20", " ")`
- 或者直接使用getResourceAsStream方法，可以避免这个问题

## BIO
一个Acceptor阻塞式获取socket连接，然后线程池阻塞式等待socket读事件，处理业务逻辑，最后写回
每个HTTP连接结束后由客户端关闭TCP连接

使用JMeter进行压力测试：connection:close

2个线程，每个线程循环访问10000次，吞吐量为478个请求/sec，平均响应时间为2ms
20个线程，每个线程循环访问1000次，吞吐量为240个请求/sec,平均响应时间为15ms
200个线程，每个线程循环访问100次，吞吐量为440个请求/sec,平均响应时间为346ms
1000个线程，每个线程循环访问20次，吞吐量为680个请求/sec,平均响应时间为1049ms

## NIO Reactor
多个（1个或2个）Acceptor阻塞式获取socket连接，然后多个Poller（处理器个数个）非阻塞式轮询socket读事件，检测到读事件时将socket交给线程池处理业务逻辑
实现HTTP的keep-alive（复用socket连接）

使用JMeter进行压力测试：connection:keep-alive
以下测试总请求次数都为20000次

2个线程，每个线程循环访问10000次，吞吐量为630个请求/sec，平均响应时间为2ms
20个线程，每个线程循环访问1000次，吞吐量为914个请求/sec,平均响应时间为12ms
200个线程，每个线程循环访问100次，吞吐量为737个请求/sec,平均响应时间为165ms
1000个线程，每个线程循环访问20次，吞吐量为570个请求/sec,平均响应时间为1195ms


## 未来希望添加的功能：
- 手写WebSocket服务器，实现HTTP长连接
- Filter
- Listener
- 实现Servlet3.1标准
- 实现AsyncServlet
- 实现多应用隔离，自定义类加载器体系

## 另附CSDN相关博客
http://blog.csdn.net/songxinjianqwe/article/details/75670552

![image](http://markdown-1252651195.cossh.myqcloud.com/%E6%9C%AA%E5%91%BD%E5%90%8D%E6%96%87%E4%BB%B6.jpg)