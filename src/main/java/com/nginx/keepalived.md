###模板
    ! Configuration File for keepalived
    global_defs {                                     #全局定义部分
        notification_email {                          #设置报警邮件地址，可设置多个
            acassen@firewall.loc                      #接收通知的邮件地址
        }                        
        notification_email_from test0@163.com         #设置 发送邮件通知的地址
        smtp_server smtp.163.com                      #设置 smtp server 地址，可是ip或域名.可选端口号 （默认25）
        smtp_connect_timeout 30                       #设置 连接 smtp server的超时时间
        router_id LVS_DEVEL                           #主机标识，用于邮件通知
        vrrp_skip_check_adv_addr                   
        vrrp_strict                                   #严格执行VRRP协议规范，此模式不支持节点单播
        vrrp_garp_interval 0                       
        vrrp_gna_interval 0     
        script_user keepalived_script                 #指定运行脚本的用户名和组。默认使用用户的默认组。如未指定，默认为keepalived_script 用户，如无此用户，则使用root
        enable_script_security                        #如过路径为非root可写，不要配置脚本为root用户执行。
    }       
    
    vrrp_script chk_nginx_service {                   #VRRP 脚本声明
        script "/etc/keepalived/chk_nginx.sh"         #周期性执行的脚本
        interval 3                                    #运行脚本的间隔时间，秒
        weight -20                                    #权重，priority值减去此值要小于备服务的priority值
        fall 3                                        #检测几次失败才为失败，整数
        rise 2                                        #检测几次状态为正常的，才确认正常，整数
        user keepalived_script                        #执行脚本的用户或组
    }                                             
    
    vrrp_instance VI_1 {                              #vrrp 实例部分定义，VI_1自定义名称
        state MASTER                                  #指定 keepalived 的角色，必须大写 可选值：MASTER|BACKUP
        interface ens33                               #网卡设置，lvs需要绑定在网卡上，realserver绑定在回环口。区别：lvs对访问为外，realserver为内不易暴露本机信息
        virtual_router_id 51                          #虚拟路由标识，是一个数字，同一个vrrp 实例使用唯一的标识，MASTER和BACKUP 的 同一个 vrrp_instance 下 这个标识必须保持一致
        priority 100                                  #定义优先级，数字越大，优先级越高。
        advert_int 1                                  #设定 MASTER 与 BACKUP 负载均衡之间同步检查的时间间隔，单位为秒，两个节点设置必须一样
        authentication {                              #设置验证类型和密码，两个节点必须一致
            auth_type PASS                        
            auth_pass 1111                        
        }                                         
        virtual_ipaddress {                           #设置虚拟IP地址，可以设置多个虚拟IP地址，每行一个
            192.168.119.130                       
        }
        track_script {                                #脚本监控状态
            chk_nginx_service                         #可加权重，但会覆盖声明的脚本权重值。chk_nginx_service weight -20
        }
        notify_master "/etc/keepalived/start_haproxy.sh start"  #当前节点成为master时，通知脚本执行任务
        notify_backup "/etc/keepalived/start_haproxy.sh stop"   #当前节点成为backup时，通知脚本执行任务
        notify_fault  "/etc/keepalived/start_haproxy.sh stop"   #当当前节点出现故障，执行的任务; 
    }                                             
    
    virtual_server 192.168.119.130 80  {          #定义RealServer对应的VIP及服务端口，IP和端口之间用空格隔开
        delay_loop 6                              #每隔6秒查询realserver状态
        lb_algo rr                                #后端调试算法（load balancing algorithm）
        lb_kind DR                                #LVS调度类型NAT/DR/TUN
        #persistence_timeout 60                   同一IP的连接60秒内被分配到同一台realserver
        protocol TCP                              #用TCP协议检查realserver状态
        real_server 192.168.119.120 80 {          
            weight 1                              #权重，最大越高，lvs就越优先访问
            TCP_CHECK {                           #keepalived的健康检查方式HTTP_GET | SSL_GET | TCP_CHECK | SMTP_CHECK | MISC
                connect_timeout 10                #10秒无响应超时
                retry 3                           #重连次数3次
                delay_before_retry 3              #重连间隔时间
                connect_port 80                   #健康检查realserver的端口
            }                                     
        }                                         
        real_server 192.168.119.121 80 {          
            weight 1                              #权重，最大越高，lvs就越优先访问
            TCP_CHECK {                           #keepalived的健康检查方式HTTP_GET | SSL_GET | TCP_CHECK | SMTP_CHECK | MISC
                connect_timeout 10                #10秒无响应超时
                retry 3                           #重连次数3次
                delay_before_retry 3              #重连间隔时间
                connect_port 80                   #健康检查realserver的端口
            }                                     
        }                                         
    }                                             
    
    vrrp_instance VI_2 {                          #vrrp 实例部分定义，VI_1自定义名称
        state   BACKUP                            #指定 keepalived 的角色，必须大写 可选值：MASTER|BACKUP 分别表示（主|备）
        interface ens33                           #网卡设置，绑定vip的子接口，lvs需要绑定在网卡上，realserver绑定在回环口。区别：lvs对访问为外，realserver为内不易暴露本机信息
        virtual_router_id 52                      #虚拟路由标识，是一个数字，同一个vrrp 实例使用唯一的标识，MASTER和BACKUP 的 同一个 vrrp_instance 下 这个标识必须保持一致
        priority 90                               #定义优先级，数字越大，优先级越高。
        advert_int 1                              #设定 MASTER 与 BACKUP 负载均衡之间同步检查的时间间隔，单位为秒，两个节点设置必须一样
        authentication {                          #设置验证类型和密码，两个节点必须一致
            auth_type PASS                        
            auth_pass 1111                        
        }                                         
        virtual_ipaddress {                       #设置虚拟IP地址，可以设置多个虚拟IP地址，每行一个
            192.168.119.131                       
        }                                         
    }                                             
    
    virtual_server 192.168.119.131 80 {           #定义RealServer对应的VIP及服务端口，IP和端口之间用空格隔开
        delay_loop 6                              #每隔6秒查询realserver状态
        lb_algo rr                                #后端调试算法（load balancing algorithm）
        lb_kind DR                                #LVS调度类型NAT/DR/TUN
        #persistence_timeout 60                   #同一IP的连接60秒内被分配到同一台realserver
        protocol TCP                              #用TCP协议检查realserver状态
        real_server 192.168.119.120 80 {          
            weight 1                              #权重，最大越高，lvs就越优先访问
            TCP_CHECK {                           #keepalived的健康检查方式HTTP_GET | SSL_GET | TCP_CHECK | SMTP_CHECK | MISC
                connect_timeout 10                #10秒无响应超时
                retry 3                           #重连次数3次
                delay_before_retry 3              #重连间隔时间
                connect_port 80                   #健康检查realserver的端口
            }                                     
        }                                         
        real_server 192.168.119.121 80 {          
            weight 1                              #权重，最大越高，lvs就越优先访问
            TCP_CHECK {                           #keepalived的健康检查方式HTTP_GET | SSL_GET | TCP_CHECK | SMTP_CHECK | MISC
                connect_timeout 10                #10秒无响应超时
                retry 3                           #重连次数3次
                delay_before_retry 3              #重连间隔时间
                connect_port 80                   #健康检查realserver的端口
            }
        }
    }

            default_interface eth0：设置静态地址默认绑定的端口。默认是eth0。

            lvs_sync_daemon <INTERFACE> <VRRP_INSTANCE> [id <SYNC_ID>] [maxlen <LEN>] [port <PORT>] [ttl <TTL>] [group <IP ADDR>]
                设置LVS同步服务的相关内容。可以同步LVS的状态信息。
                INTERFACE：指定同步服务绑定的接口。
                VRRP_INSTANCE：指定同步服务绑定的VRRP实例。
                id <SYNC_ID>：指定同步服务所使用的SYNCID，只有相同的SYNCID才会同步。范围是0-255.
                maxlen：指定数据包的最大长度。范围是1-65507
                port：指定同步所使用的UDP端口。
                group：指定组播IP地址。

            lvs_flush：在keepalived启动时，刷新所有已经存在的LVS配置。
            vrrp_garp_master_delay 10：当转换为MASTER状态时，延迟多少秒发送第二组的免费ARP。默认为5s，0表示不发送第二组免的免费ARP。
            vrrp_garp_master_repeat 1：当转换为MASTER状态时，在一组中一次发送的免费ARP数量。默认是5.
            vrrp_garp_lower_prio_delay 10：当MASTER收到更低优先级的通告时，延迟多少秒发送第二组的免费ARP。
            vrrp_garp_lower_prio_repeat 1：当MASTER收到更低优先级的通告时，在一组中一次发送的免费ARP数量。
            vrrp_garp_master_refresh 60：当keepalived成为MASTER以后，刷新免费ARP的最小时间间隔(会再次发送免费ARP)。默认是0，表示不会刷新。
            vrrp_garp_master_refresh_repeat 2： 当keepalived成为MASTER以后，每次刷新会发送多少个免费ARP。默认是1.
            vrrp_garp_interval 0.001：在一个接口发送的两个免费ARP之间的延迟。可以精确到毫秒级。默认是0.
            vrrp_lower_prio_no_advert true|false：默认是false。如果收到低优先级的通告，不发送任何通告。
            vrrp_version 2|3：设置默认的VRRP版本。默认是2.
            vrrp_check_unicast_src：在单播模式中，开启对VRRP数据包的源地址做检查，源地址必须是单播邻居之一。
            vrrp_skip_check_adv_addr：默认是不跳过检查。检查收到的VRRP通告中的所有地址可能会比较耗时，设置此命令的意思是，如果通告与接收的上一个通告来自相同的master路由器，则不执行检查(跳过检查)。
            vrrp_strict：严格遵守VRRP协议。下列情况将会阻止启动Keepalived：1. 没有VIP地址。2. 单播邻居。3. 在VRRP版本2中有IPv6地址。

            vrrp_iptables：不添加任何iptables规则。默认是添加iptables规则的。

            如果vrrp进程或check进程超时，可以用下面的4个选项。可以使处于BACKUP状态的VRRP实例变成MASTER状态，即使MASTER实例依然在运行。因为MASTER或BACKUP系统比较慢，不能及时处理VRRP数据包。
            vrrp_priority <-20 -- 19>：设置VRRP进程的优先级。
            checker_priority <-20 -- 19>：设置checker进程的优先级。
            vrrp_no_swap：vrrp进程不能够被交换。
            checker_no_swap：checker进程不能够被交换。

            script_user <username> [groupname]：设置运行脚本默认用户和组。如果没有指定，则默认用户为keepalived_script(需要该用户存在)，否则为root用户。默认groupname同username。
            enable_script_security：如果脚本路径的任一部分对于非root用户来说，都具有可写权限，则不会以root身份运行脚本。
            nopreempt 默认是抢占模式 要是用非抢占式的就加上nopreempt
            注意：上述为global_defs中的指令
##### VRRPD配置
    VRRPD的配置包括如下子块：
        vrrp_script
        vrrp_sync_group
        garp_group
        vrrp_instance
    vrrp_script配置
        作用：添加一个周期性执行的脚本。脚本的退出状态码会被调用它的所有的VRRP Instance记录。
        注意：至少有一个VRRP实例调用它并且优先级不能为0.优先级范围是1-254.
        vrrp_script <SCRIPT_NAME> {
                  ...
            }

        选项说明：
        script "/path/to/somewhere"：指定要执行的脚本的路径。
        interval <INTEGER>：指定脚本执行的间隔。单位是秒。默认为1s。
        timeout <INTEGER>：指定在多少秒后，脚本被认为执行失败。
        weight <-254 --- 254>：调整优先级。默认为2.
        rise <INTEGER>：执行成功多少次才认为是成功。
        fall <INTEGER>：执行失败多少次才认为失败。
        user <USERNAME> [GROUPNAME]：运行脚本的用户和组。
        init_fail：假设脚本初始状态是失败状态。

        解释：
        weight：
        1. 如果脚本执行成功(退出状态码为0)，weight大于0，则priority增加。
        2. 如果脚本执行失败(退出状态码为非0)，weight小于0，则priority减少。
        3. 其他情况下，priority不变。
    vrrp_instance
        命令说明：
        state MASTER|BACKUP：指定该keepalived节点的初始状态。
        interface eth0：vrrp实例绑定的接口，用于发送VRRP包。
        use_vmac [<VMAC_INTERFACE>]：在指定的接口产生一个子接口，如vrrp.51，该接口的MAC地址为组播地址，通过该接口向外发送和接收VRRP包。
        vmac_xmit_base：通过基本接口向外发送和接收VRRP数据包，而不是通过VMAC接口。
        native_ipv6：强制VRRP实例使用IPV6.(当同时配置了IPV4和IPV6的时候)
        dont_track_primary：忽略VRRP接口的错误，默认是没有配置的。

        track_interface {
          eth0
          eth1 weight <-254-254>
          ...
        }：如果track的接口有任何一个出现故障，都会进入FAULT状态。

        track_script {
          <SCRIPT_NAME>
          <SCRIPT_NAME> weight <-254-254>
        }：添加一个track脚本(vrrp_script配置的脚本。)

        mcast_src_ip <IPADDR>：指定发送组播数据包的源IP地址。默认是绑定VRRP实例的接口的主IP地址。
        unicast_src_ip <IPADDR>：指定发送单薄数据包的源IP地址。默认是绑定VRRP实例的接口的主IP地址。
        version 2|3：指定该实例所使用的VRRP版本。

        unicast_peer {
           <IPADDR>
           ...
        }：采用单播的方式发送VRRP通告，指定单播邻居的IP地址。

        virtual_router_id 51：指定VRRP实例ID，范围是0-255.
        priority 100：指定优先级，优先级高的将成为MASTER。
        advert_int 1：指定发送VRRP通告的间隔。单位是秒。
        authentication {
          auth_type PASS|AH：指定认证方式。PASS简单密码认证(推荐),AH:IPSEC认证(不推荐)。
          auth_pass 1234：指定认证所使用的密码。最多8位。
        }

        virtual_ipaddress {
           <IPADDR>/<MASK> brd <IPADDR> dev <STRING> scope <SCOPE> label <LABEL>
           192.168.200.17/24 dev eth1
           192.168.200.18/24 dev eth2 label eth2:1
        }：指定VIP地址。

        nopreempt：设置为不抢占。默认是抢占的，当高优先级的机器恢复后，会抢占低优先级的机器成为MASTER，而不抢占，则允许低优先级的机器继续成为MASTER，即使高优先级的机器已经上线。如果要使用这个功能，则初始化状态必须为BACKUP。
        preempt_delay：设置抢占延迟。单位是秒，范围是0---1000，默认是0.发现低优先级的MASTER后多少秒开始抢占。


    通知脚本：
        notify_master <STRING>|<QUOTED-STRING> [username [groupname]]
        notify_backup <STRING>|<QUOTED-STRING> [username [groupname]]
        notify_fault <STRING>|<QUOTED-STRING> [username [groupname]]
        notify <STRING>|<QUOTED-STRING> [username [groupname]]

        # 当停止VRRP时执行的脚本。
        notify_stop <STRING>|<QUOTED-STRING> [username [groupname]]
        smtp_alert
        vrrp_sync_group
        作用：将所有相关的VRRP实例定义在一起，作为一个VRRP Group，如果组内的任意一个实例出现问题，都可以实现Failover。
         vrrp_sync_group VG_1 {
            group {
             inside_network     # vrrp instance name
             outside_network    # vrrp instance name
             ...
            }
            ...
        }

        说明：
        如果username和groupname没有指定，则以默认的script_user所指定的用户和组。
        1. notify_master /path/to_master.sh [username [groupname]]
            作用：当成为MASTER时，以指定的用户和组执行脚本。
        2. notify_backup /path/to_backup.sh [username [groupname]]
            作用：当成为BACKUP时，以指定的用户和组执行脚本。
        3. notify_fault "/path/fault.sh VG_1" [username [groupname]]
            作用：当该同步组Fault时，以指定的用户和组执行脚本。
        4. notify /path/notify.sh [username [groupname]]
            作用：在任何状态都会以指定的用户和组执行脚本。
            说明：该脚本会在notify_*脚本后执行。
            notify可以使用3个参数，如下：
            $1：可以是GROUP或INTANCE，表明后面是组还是实例。
            $2：组名或实例名。
            $3：转换后的目标状态。有：MASTER、BACKUP、FAULT。

        5. smtp_alert：当状态发生改变时，发送邮件。
        6. global_tracking：所有的VRRP实例共享相同的tracking配置。

        注意：脚本文件要加上x权限，同时指令最好写绝对路径。
    LVS配置
        LVS模块结构：

        virtual_server{
            … ...
            real_server{
                … ...
            }
        }
    virtual_server
        virtual_server IP Port | virtual_server fwmark int | virtual_server group string {
          delay_loop <INT>：健康检查的时间间隔。
          lb_argo rr|wrr|lc|wlc|lblc|sh|dh：LVS调度算法。
          lb_kind NAT|DR|TUN：LVS模式。
          persistence_timeout 360：持久化超时时间，单位是秒。默认是6分钟。
          persistence_granularity：持久化连接的颗粒度。
          protocol TCP|UDP|SCTP：4层协议。
          ha_suspend：如果virtual server的IP地址没有设置，则不进行后端服务器的健康检查。
          virtualhost <STRING>：为HTTP_GET和SSL_GET执行要检查的虚拟主机。如virtualhost www.felix.com
          sorry_server <IPADDR> <PORT>：添加一个备用服务器。当所有的RS都故障时。
          sorry_server_inhibit：将inhibit_on_failure指令应用于sorry_server指令。

          alpha：在keepalived启动时，假设所有的RS都是down，以及健康检查是失败的。有助于防止启动时的误报。默认是禁用的。
          omega：在keepalived终止时，会执行quorum_down指令所定义的脚本。

          quorum <INT>：默认值1. 所有的存活的服务器的总的最小权重。
          quorum_up <STRING>：当quorum增长到满足quorum所定义的值时，执行该脚本。
          quorum_down <STRING>：当quorum减少到不满足quorum所定义的值时，执行该脚本。
        }
    real_server
        real_server IP Port {
          weight <INT>：给服务器指定权重。默认是1.
          inhibit_on_failure：当服务器健康检查失败时，将其weight设置为0，而不是从Virtual Server中移除。
          notify_up <STRING>：当服务器健康检查成功时，执行的脚本。
          notify_down <STRING>：当服务器健康检查失败时，执行的脚本。
          uthreshold <INT>：到这台服务器的最大连接数。
          lthreshold <INT>：到这台服务器的最小连接数。
        }
    real_server监控检查
        HTTP_GET | SSL_GET {
            url {
              path <STRING>：指定要检查的URL的路径。如path / or path /mrtg2
              digest <STRING>：摘要。计算方式：genhash -s 172.17.100.1 -p 80 -u /index.html
              status_code <INT>：状态码。
            }
            nb_get_retry <INT>：get尝试次数。
            delay_before_retry <INT>：在尝试之前延迟多长时间。

            connect_ip <IP ADDRESS>：连接的IP地址。默认是real server的ip地址。
            connect_port <PORT>：连接的端口。默认是real server的端口。
            bindto <IP ADDRESS>：发起连接的接口的地址。
            bind_port <PORT>：发起连接的源端口。
            connect_timeout <INT>：连接超时时间。默认是5s。
            fwmark <INTEGER>：使用fwmark对所有出去的检查数据包进行标记。
            warmup <INT>：指定一个随机延迟，最大为N秒。可防止网络阻塞。如果为0，则关闭该功能。
        }

        TCP_CHECK {
            connect_ip <IP ADDRESS>：连接的IP地址。默认是real server的ip地址。
            connect_port <PORT>：连接的端口。默认是real server的端口。
            bindto <IP ADDRESS>：发起连接的接口的地址。
            bind_port <PORT>：发起连接的源端口。
            connect_timeout <INT>：连接超时时间。默认是5s。
            fwmark <INTEGER>：使用fwmark对所有出去的检查数据包进行标记。
            warmup <INT>：指定一个随机延迟，最大为N秒。可防止网络阻塞。如果为0，则关闭该功能。
            retry <INIT>：重试次数。默认是1次。
            delay_before_retry <INT>：默认是1秒。在重试之前延迟多少秒。
        }


        SMTP_CHECK {
            connect_ip <IP ADDRESS>：连接的IP地址。默认是real server的ip地址。
            connect_port <PORT>：连接的端口。默认是real server的端口。 默认是25端口
            bindto <IP ADDRESS>：发起连接的接口的地址。
            bind_port <PORT>：发起连接的源端口。
            connect_timeout <INT>：连接超时时间。默认是5s。
            fwmark <INTEGER>：使用fwmark对所有出去的检查数据包进行标记。
            warmup <INT>：指定一个随机延迟，最大为N秒。可防止网络阻塞。如果为0，则关闭该功能。

            retry <INT>：重试次数。
            delay_before_retry <INT>：在重试之前延迟多少秒。
            helo_name <STRING>：用于SMTP HELO请求的字符串。
        }


        DNS_CHECK {
            connect_ip <IP ADDRESS>：连接的IP地址。默认是real server的ip地址。
            connect_port <PORT>：连接的端口。默认是real server的端口。 默认是25端口
            bindto <IP ADDRESS>：发起连接的接口的地址。
            bind_port <PORT>：发起连接的源端口。
            connect_timeout <INT>：连接超时时间。默认是5s。
            fwmark <INTEGER>：使用fwmark对所有出去的检查数据包进行标记。
            warmup <INT>：指定一个随机延迟，最大为N秒。可防止网络阻塞。如果为0，则关闭该功能。

            retry <INT>：重试次数。默认是3次。
            type <STRING>：DNS query type。A/NS/CNAME/SOA/MX/TXT/AAAA
            name <STRING>：DNS查询的域名。默认是(.)
        }



        MISC_CHECK {
             misc_path <STRING>：外部的脚本或程序路径。
             misc_timeout <INT>：脚本执行超时时间。
             user USERNAME [GROUPNAME]：指定运行该脚本的用户和组。如果没有指定GROUPNAME，则GROUPNAME同USERNAME。
             misc_dynamic：根据退出状态码动态调整权重。
              0，健康检查成功，权重不变。
              1，健康检查失败。
              2-255，健康检查成功。权重设置为退出状态码减去2.如退出状态码是250，则权重调整为248
               warmup <INT>：指定一个随机延迟，最大为N秒。可防止网络阻塞。如果为0，则关闭该功能。
        }