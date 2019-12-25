#### 模块配置
修改pom.xml中parent中的version为1.5.10，即使用spring boot1.x的版本。添加通用mapper的坐标,
#### spring boot1.x与spring boot2.x版本的区别
spring boot1.x遵循spring4.0的规则，因为Spring与springMVC为父子容器关系，所以springMVC的版本也为4.0。自带的tomcat版本为8，对thymeleaf支持有变化，html标签必须为双标签

spring boot2.x遵循spring5.0的规则，因为Spring与springMVC为父子容器关系，所以springMVC的版本也为5.0。自带的tomcat版本为9，
#### spring4.0与spring5.0的区别
spring4.0：拦截器实现方式有：继承和实现两种方式。

spring5.0：拦截器实现方式有：要求JDK版本最低为Java8，支持JUnit5并且兼容JUnit4，支持函数式编程。
#### 功能开发流程
第一步(数据访问层)：从数据库中找对应的表，创建对应的bean，然后写interface，再写mapper(使用通用mapper可以把interface和mapper合并)

第二步(服务层)：创建service与serviceImpl 

第三步(控制层(承载数据模型与页面渲染))：创建控制器controller 
#### 通用mapper的使用
1. 添加通用mapper的坐标，添加依赖

2. 创建实体类

3. 创建接口并继承通用mapper(泛型为实体类)

4. 在启动类添加mapper扫描！！！

5. 编写服务层及实现类，实现类调用mapper

6. 编写控制器，控制器调用服务层

7. 配置数据源！！！

8. 测试
##### 通用mapper的用法
select(UserInfo userInfo) 相当于属性的等值查询

selectOne(UserInfo userInfo) 登录

selectAll() 查询所有

selectByExample(Object example) 查询范围数据或等值数据

selectByPrimaryKey() 根据主键查询

insertSelective(userInfo) 选择性的添加

delete(UserInfo userInfo) 等值删除

deleteByPrimaryKey(Object key) 根据主键删除

deleteByExample(Object example) 范围或等值删除

updateByExampleSelective(userInfo, example) 根据条件修改(第一个参数表示要修改的数据，第二个参数表示修改的条件)








