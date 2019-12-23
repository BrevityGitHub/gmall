## Linux命令(centos7)
passwd 修改用户密码

pwd 显示当前路径

ifconfig 显示网卡信息

vim /etc/sysconfig/network-scripts/ifcfg-ens33 修改网络文件，配置静态IP、DNS等

service network restart 重启网络服务

ping 域名 测试网络是否通畅

systemctl disable firewalld 永久禁用防火墙

systemctl stop firewalld 停止防火墙服务

systemctl status firewalld 查看防火墙状态

history 显示历史操作

mkdir 创建文件夹

rpm -e --nodeps 文件名 卸载rpm方式安装的文件

rpm -ivh 文件名.rpm 安装rpm文件

Linux下文件的权限类型一般包括读，写，执行。对应字母为 r=4、w=2、x=1。

若要同时设置 rwx (可读写运行)权限则将该权限位设置为 4 + 2 + 1 = 7

若要同时设置 rw- (可读写不可运行)权限则将该权限位设置为 4 + 2 = 6

若要同时设置 r-x (可读可运行不可写)权限则将该权限位设置为 4 +1 = 5

chmod -R 777 文件夹 授予文件夹所有的权限

### 安装mysql
rpm -ivh mysql-community-common- …….rpm

rpm -ivh mysql-community-libs- …… .rpm

rpm -ivh mysql-community-client- …… .rpm

rpm -ivh mysql-community-server- …… .rpm

mysqladmin --version 查看mysql的版本

mysqld --initialize --user=mysql 初始化mysql

cat /var/log/mysqld.log 查看mysql初始化的密码(root@localhost: 后面就是初始化的密码)

systemctl start mysqld.service 启动mysql服务

### 虚拟机不能上网的解决方法
先看Linux的网络配置文件是否正确，再看宿主机的VMware的所有服务是否开启(注意：安装虚拟机之前必须先启动VMware的所有服务，否则虚拟机永远连不上外网)
