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

### 4. 版本和Maven依赖

    <dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan</artifactId>
		<version>1.0.0</version>
	</dependency>

### 5. 代码片段

	
	<sql-service id="updateProjectP2" dsKey="writetvr" txRef="tx_02">
		<if test="{through} == 2">
			<update rowCount="{nCount}">
				update project set
					project_state = 25, 
					audit_time =  #{audit_time|now()},
					update_time = #{update_time|now()}
				where 
					project_sn = #{project_sn} AND 
					project_ctrl_state = 1 AND 
					project_state = 20
			</update>
			<exception test="{nCount} != 1" code="-1" message="项目审核失败"/>
			
			<selectOne resultKey="{project}">
				select * from project where project_sn = #{project_sn}
			</selectOne>
			<if test="{project.reservation_mode} == 2">
				<insert>
					INSERT INTO project_apply (
						project_sn, provider_id, provider_name, bidding_amount,
						create_time, apply_state, reservation_state
					) VALUES (
						#{project_sn}, #{project.provider_id}, #{project.provider_name}, #{bidding_amount|0}, 
						#{create_time|now()}, 2, 10
					)							
				</insert>			
			</if>
		</if>
		<else>
			<update rowCount="{nCount}">
				update project set
					task_ctrl_state = 2, 
					audit_no_time = #{audit_no_time|now()},
					update_time = #{update_time|now()}
				where 
					project_sn = #{project_sn} AND 
					task_ctrl_state = 1
			</update>
		</else>
	</sql-service>


### 6. 使用教程

[使用教程和技术设计](https://github.com/xsonorg/tangyuan/wiki/Tutorials-and-design) 

### 7. 联系交流

- QQ群：518522232 *请备注关注的项目
- 邮箱：<xson_org@126.com>
- 项目地址: <https://github.com/xsonorg/tangyuan>