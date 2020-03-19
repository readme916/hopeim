# 数据结构查看器


* 提供给前后端同学在开发环境中使用，数据结构的详细描述

* 界面和配置只在oauth的应用中

* 启动的配置,show参数代表是否暴露接口（生成环境应该关闭），structure表示查询地址

```
spring:
  jpa:
    restful:
      structure: /structure
      show: true

```

* 搭配[REST查询](https://github.com/readme916/spring-jpa-mysql-smart-query)，食用效果更佳