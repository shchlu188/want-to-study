##Nginx的配置
####分类
    全局块:
        主要设置一些影响nginx整体运行的配置指令
    events块:
        主要影响与用户网络的连接
    http块:
        http全局块：
            ·配置包括文件引入、MIME-TYPE定义、日志自定义、连接超时时间、单连接请求书上限等。
        server块：
            
    
    
    
####反向代理:
    server {
            listen       80;
            server_name  192.168.220.135; //请求服务地址
    
            #charset koi8-r;
    
            #access_log  logs/host.access.log  main;
    
            location / {
    			 root	html;
                 index  index.html index.htm;
                 proxy_pass http://127.0.0.1:8080; // 代理ip地址
            }
            #error_page  404              /404.html;
    
            # redirect server error pages to the static page /50x.html
            #
            error_page   500 502 503 504  /50x.html;
            location = /50x.html {
                root   html;
            }        
    
####负载均衡:
    upstream myserver {
            server 192.168.220.135:8080;
            server 192.168.220.135:8081;
    
    
      }
        #gzip  on;
    
        server {
            listen       80;
            server_name  192.168.220.135;
    
            #charset koi8-r;
    
            #access_log  logs/host.access.log  main;
    
            location / {
                root   html;
                index  index.html index.htm;
                proxy_pass http://myserver;
            }
####分配策略:
    轮询（默认）
    权重：用weight配置
        upstream myserver {
                server 192.168.220.135:8080 weight=10;
                server 192.168.220.135:8081 weight=5;
          }

    ip hash: 按照IP的hash的结果分配
        upstream myserver {
                ip_hash;
                server 192.168.220.135:8080;
                server 192.168.220.135:8081;
          }
    响应时间分配: 响应时间越短优先分配(第三方)
        upstream myserver {
                server 192.168.220.135:8080;
                server 192.168.220.135:8081; 
                fair;        
                 }


    
####动静分离:
    动静分离的方案：
        ·纯粹地把静态文件独立成单独的域名，挡在独立的服务器上；（目前主流方案）
        ·动态和静态资源混合在一起，通过nginx来分开；
        
    通过location指定不同的后缀名实现不同的请求转发。通过expires参数来
    设置浏览器缓存过期的时间，此设置适合不经常发生变动的资源；
    server {
            listen       80;
            server_name  localhost;
            location /www/ { #静态资源
                root   /data/;
                index  index.html index.htm;
            }
            location /image/ { #静态资源
                root   /data/;
                autoindex on;
            }
            }
####高可用集群:
    配置nginx集群的原因:
        nginx可能出现宕机，请求无法实现效果
    需要的环境:
        1、两台或多台nginx服务器
        2、需要keepalived: yum install keepalived -y
              配置keepalived.config文件
              三部分：
                global_defs #全局定义部分
                vrrp_script chk_nginx_service #VRRP 脚本声明
                vrrp_instance VI_1 #虚拟IP配置
    
    
****####nginx原理:
    master&woker
        client发送请求----->master---->woker之间进行争抢机制,得到任务进行反向代理;
    一个master和多个worker的优点：
        1、可以使用nginx -s reload 热部署，
        2、对于每个worker都是独立的进程，不需要加锁，省掉了锁的消耗；如果有一个worker出现问题，
            由于worker之间是独立的，继续进行争抢，实现请求过程，不会造成服务中断。
    设置多少worker合适:
        同redis类似都采用了多路复用机制，由于worker是独立的进程，每个进程只有一个主线程，通过异步
        非阻塞的方式来处理请求，每个woker的线程都能把cpu的性能发挥到极致。因此worker数和服务器的cpu
        数向等最为适宜，设置少了浪费资源，多了会造成cpu频繁切换上下文带来资源的损耗。
    
    连接数 worker connection
        发送一个请求占用worker的几个连接数？
            2个或者4个；
            2个：master-->worker-->服务器
            4个：master-->worker-->服务器  master<--worker<--服务器
        nginx有一个master，有四个worker，每个worker支持最大的连接数据为1024，支持最大的并发数为？
            普通的静态访问最大连接数 worker connection*worker process/2
            如果http是作为反向代理而言 worker connection*worker process /4