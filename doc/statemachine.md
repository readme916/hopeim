状态机介绍
--------------
* 状态机的概念和用法，自行百度

* 状态机框架，基于[Spring StateMachine](https://spring.io/projects/spring-statemachine#overview)

* 我们进行了细微的功能扩展

* 提高了状态机的可配置性和权限划分

* 进行了可视化扩展

状态机的适用范畴
-------------

* 所有具有生命周期性质的业务，都可以使用状态机模型

* 所有具有定时器性质的业务，都可以使用状态机模型

* 所有基于角色-操作的业务，都可以使用状态机模型


数据表
-------------
状态机涉及到的数据表包括 state ， event ， timer ，role


关系图（用户生命周期，简单例子）
----------------------

* 实体状态  -> 当前状态可执行动作 -> 这个动作可执行的角色
![关系图](20200319174340.png)



状态机的代码样例
------------------

* 目标Entity实现了IStateMachineEntity接口

```java

@Entity
@Table(name = "user" , uniqueConstraints= {@UniqueConstraint(columnNames= {"union_id"})})
@EntityListeners(AuditingEntityListener.class)
public class User implements IStateMachineEntity,IRegionEntity,IDepartmentEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="uuid")
	private Long uuid;
	
```

* 实现一个对应的服务

```java

@Service
public class UserService extends StateMachineService<User> {

```



* 在控制器或者服务中调用

```java

@RestController
@RequestMapping("/v1/user")
@Api(tags = "用户的接口")
public class V1UserController extends DefaultHandler {

	@Autowired
	private UserService userService;

	@PostMapping(path = "/{uuid}/kick")
	@ApiOperation(value = "踢下线", notes = "如果用户在线则直接踢下线", httpMethod = "POST")
	public HttpPostReturnUuid kick(@PathVariable(required = true) Long uuid) {
		userService.dispatchEvent(uuid, "kick");
		return new HttpPostReturnUuid(uuid);
	}
	
```
