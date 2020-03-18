hopeim springboot后台版本
===============================


项目介绍
-----------------------------------

<h3 align="center">Java framework for enterprise web applications</h3>

hopeim 是最新一代基于事务，人，任务的交互和反馈的工作流协作基础框架，支持多种终端，表现形式主要模仿微信体系，使用者可以用来开发企业和平台类应用！


开发理念
------------------------------------
- 尽量使用大家常用和熟悉的框架和中间件
- 尽量少的引入外部依赖
- 尽量少的引入各种概念
- 尽量使用spring系列的产品线
- 尽量少写代码，只写必要代码
- 尽量少的编写大而全的流程很长，逻辑复杂，规则很多的框架模块
- 尽量使框架模块小而精，随用随取，功能正交不重叠，相互不耦合
- 尽量使代码逻辑直观而朴实，保证其他人能看懂
- 尽量做到内部功能的可配置，可视化，减少系统的黑盒效应
- 功能模块设计要求简单易懂，符合人类直觉，符合开发者得传统和习惯


适用项目
------------------------------------
本款快速开发框架，尤其适合企业的业务流程管控和时效管理，以及各种类型的业务信息的内部流转和沟通，使企业的日常运转更加顺畅和标准化
 
 
技术架构
------------------------------------
- 语言：Java 8

- 框架：Spring Boot

- 依赖管理：Maven

- 数据库：MySQL5.7+ 

- 缓存：Redis

- 任务：Quartz

- 状态机：[Spring StateMachine](https://spring.io/projects/spring-statemachine#overview)

- 登录：Spring Security Oauth2

- 持久层：Spring Data Jpa

- REST查询：[Mysql Smart Query](https://github.com/readme916/spring-jpa-mysql-smart-query)

- 连接池：Druid

- 文件存储：MongoDB

- 文档：Swagger2

- IM：  [腾讯即时通信 IM](https://cloud.tencent.com/document/product/269)


框架特色
------------------------------------
* 1.查询数据库，拼接REST字符串，使用Mysql Smart Query，开发效率很高
* 2.使用Spring data jpa的强大的数据模型管理功能，使数据库模型的修改和维护风险最低
* 3.内置数据结构：用户、角色、菜单、组织、部门、产品、订单
* 4.采用maven分模块开发方式，默认包括登录模块，用户模块，企业模块三大独立应用
* 5.提供单点登录Oauth2解决方案，支持不同客户端的password模式和code模式，用于本身登录和第三方登录
* 6.权限控制采用 RBAC（Role-Based Access Control，基于角色的访问控制）
* 7.完善的基于不同角色的菜单和操作控制机制
* 8.完善的基于不同企业的自动数据流筛选
* 9.集成Spring StateMachine，并实现了只需在后台配置流程转向，可极大的简化工作流的开发
* 9.可配置流程设计，自定义状态，事件，后果，定时器，实现流程和时效的管控。并且实现配置效果可视化
* 10.消息体系，使用微信的TIM系统，完成多终端的消息推送和事务流转
* 11.数据变更记录日志，可记录数据每次操作和变动，通过版本对比功能查看历史变化
 

使用教程
---------------------
* [maven依赖](doc/maven.md)
* [数据库设计](doc/db.md)
* [地理数据](doc/region.md)
* [Oauth2登录](doc/login.md)
* [多租户系统](doc/rent.md)
* [标准列表页控制器](doc/list.md)
* [标准详细页控制器](doc/detail.md)
* [角色权限](doc/authority.md)
* [限流控制](doc/ratelimiter.md)
* [文件上传](doc/file.md)
* [REST查询](https://github.com/readme916/spring-jpa-mysql-smart-query)
* [异常和事务](doc/exception.md)
* [状态机使用](doc/statemachine.md)
* [TIM使用](doc/tim.md)

备注
----

> 有任何问题可以联系开发者。
