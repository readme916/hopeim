列表页控制器
-----------
* 企业端通用数据列表控制器DetailController /v1/detail/{entity}/{uuid},数据实现了IOrgEntity接口 ，**控制器会自动筛选本企业的数据**,并且只有普通属性

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