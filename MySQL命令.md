### MySQL常用命令
mysql -h IP地址 -P 3306 -u 用户名 -p 密码

root用户远程授权给所有人：grant all privileges on *.* to root@'%' identified by 'root';

授权后刷新权限：flush privileges;

create database 数据库名; 创建数据库

use 数据库名; 切换数据库

show databases; 显示数据库

show tables; 显示表


