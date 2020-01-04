### redis
redis是一个开源的、用C语言写的高性能的分布式内存数据库，基于内存运行，支持持久化，即可以将内存中的数据保存到磁盘中，
重启的时候可以再次加载使用。redis支持数据的备份，即主从模式的数据备份。

redis支持异步持久化，即将数据写到磁盘上的同时不影响服务的正常运行。

redis默认有16个库(0-15)，使用统一的密码管理，默认使用0号库，默认端口为6379。

redis服务启动后，可以使用redis-benchmark测试机器性能，redis-check-aof：修复有问题的AOF文件，
redis-check-dump：修复有问题的dump.rdb文件，redis-cli：客户端操作入口，redis-cli shutdown 单实例关闭，
redis-cli -p 6379 shutdown 多实例关闭，指定端口关闭。
#### redis命令(没有分号)
select 库编号 切换数据库

dbsize 查看当前库的key的数量

flushdb 清空当前库

flushall 清空所有库

keys * 查询当前库的所有数据

exists key的名字，判断某个key是否存在
 
expire key 秒：为给定的key设置过期时间为多少秒

ttl key 查看key还有多少秒过期，-1表示永不过期，-2表示已过期  

type key 查看key是什么类型

del key 删除这个键
#### redis的五种数据类型及使用场景(redis5.0后增加了一个stream数据类型)
String：最基本的类型，一个key对应一个value，是二进制安全的，可以存储任何数据，value最多可以是512M。一般使用时只存储常量、变量

List：简单的字符串列表，按照插入顺序排序，底层是双向链表。可以用于做简单的队列处理

Set：无序的集合。一般用于获取两个数据集的交集、补集、差集

Hash(类似于java的Map)：是一个键值对集合，value还是一个键值对。一般用于存用户对象(便于修改)

Zset：有序的集合，不允许重复的元素，每个元素都会关联一个double类型的分数，但是分数可以重复。一般用于排序