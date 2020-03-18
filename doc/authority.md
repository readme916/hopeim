角色说明
--------------
* 每个用户只有一个角色
* 系统全局默认的两个角色 ： developer（开发者），user（普通用户） 
* 企业端引入的默认的三个角色：platform_super（超管），platform_manager（管理员），platform_employee（员工）

菜单权限
--------------

* 企业端的菜单列表，根据每个登录用户的角色，返回自己的菜单，菜单是个**多根树**

* 企业端菜单列表获取地址：/v1/list/menu


动作权限
------------

* 框架完全兼容Spring Security，所以可以使用标准的Spring Security的方式来控制权限

* 如果Entity实现了IStateMachineEntity接口，则可以使用可配置的，框架自带的[状态机体系](statemachine.md)来管理权限

* 如果使用[标准详细页控制器](detail.md)得到状态机接口数据，会自动注入当前用户可操作的events到对象map中，方便前端使用



数据权限
------------
* 企业端的[标准列表页控制器](list.md)自动根据企业id筛选数据

* 其他规则可自定义路由实现