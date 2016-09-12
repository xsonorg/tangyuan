# tangyuan

---

### 1. 项目介绍

TangYuan是一个基于Java的持久层框架。提供的持久层框架包括SQL Maps和Data Access Objects（DAO）

### 2. 项目特性

> * 数据源相关

支持多数据源，让读写分离，多数据库的应用变得简单。支持数据源组，在分库分表的大数据量应用环境将更加方便。

> * 事务相关

原生的支持事务的传播和隔离，无需依托第三方框架，同时支持多数据源的JDBC事务。

> * 缓存相关

原生提供多种缓存的的支持，并同时支持多种缓存的混合使用。

> * 数据映射相关

支持用用户自定义的配置，同时提供提供基于规则的映射配置。

> * 分库分表支持

原生的支持基于Hash、Range、Mod、Random模式的分库分表设置，同时支持用户自定义的分库分表策略。

> * 数据访问相关

支持单条的SQL语句访问，同时并支持复杂的组合SQL语句访问，让数据库的应用开发更为高效、简单。

> * Mongo访问相关

### 3. 系统架构
![系统架构图](https://github.com/xsonorg/imagedoc/blob/master/img/tangyuan.jpg)

### 4. 4. 项目结构

1. tangyuan-configuration.xml
2. tangyuan-mapper.xml
3. tangyuan-sharding.xml
4. tangyuan-sqlservices.xml

### 5. 使用教程

### 6. 设计细节
