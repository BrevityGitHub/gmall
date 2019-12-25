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
1. 添加通用mapper的坐标

2. 定义实体类

3. 定义接口并继承通用mapper(泛型为实体类)

4. 在启动类添加mapper扫描！

5. 编写服务层

6. 编写控制器

7. 配置数据源！

8. 测试

