标准详细页控制器
-----------
* 企业端通用数据详细页控制器DetailController /v1/detail/{entity}/{uuid},数据如果实现了IOrgEntity接口 ，**控制器会自动筛选本企业的数据**,并且只有普通属性

* 例如 /v1/detail/role/1

```json
{
    "createdAt": "2020-03-17 18:53:17",
    "code": "developer",
    "name": "开发者",
    "terminal": "org",
    "version": 0,
    "uuid": 1,
    "updatedAt": "2020-03-17 18:53:17"
}
```


* 如果有不同的需求，例如加入对象属性，可以自己实现路由@RequestMapping ，例子见ListController /v1/detail/user/1

```json
{
    "role": {
        "createdAt": "2020-03-17 18:53:17",
        "code": "developer",
        "name": "开发者",
        "terminal": "org",
        "version": 0,
        "uuid": 1,
        "updatedAt": "2020-03-17 18:53:17"
    },
    "unionId": "508610fcdfc6171173b01c6cd41262b3",
    "org": {},
    "sex": null,
    "uuid": 1,
    "version": 1,
    "enabled": true,
    "createdAt": "2020-03-17 19:01:01",
    "headimgurl": "https://www.gravatar.com/avatar/21232f297a57a5a743894a0e4a801fc3?d=robohash&s=256",
    "nickname": "用户gativs",
    "state": {},
    "department": {},
    "events": [],
    "updatedAt": "2020-03-17 19:01:02"
}
```

* 如果数据实现了IStateMachineEntity接口 ，则返回对象内部会自动加入events字段。即当前状态，当前用户可以执行的event，用于在详细页的显示

```java
{
    "role": {
        "createdAt": "2020-03-17 23:17:15",
        "code": "developer",
        "name": "开发者",
        "terminal": "org",
        "version": 0,
        "uuid": 1,
        "updatedAt": "2020-03-17 23:17:15"
    },
    "unionId": "2b59fa681f5ff8cd3897d20b71630a35",
    "org": {},
    "sex": null,
    "uuid": 1,
    "version": 1,
    "enabled": true,
    "createdAt": "2020-03-17 23:17:15",
    "headimgurl": "https://www.gravatar.com/avatar/21232f297a57a5a743894a0e4a801fc3?d=robohash&s=256",
    "nickname": "用户fzhxe5",
    "state": {
        "createdAt": "2020-03-17 23:18:23",
        "isChoice": false,
        "code": "created",
        "name": "初始状态",
        "sort": 10,
        "isStart": true,
        "uuid": 1,
        "isEnd": false,
        "version": 1,
        "entity": "user",
        "updatedAt": "2020-03-17 23:18:23"
    },
    "department": {},
    "events": [
        "enable",
        "disable"
    ],
    "updatedAt": "2020-03-17 23:17:16"
}
```

