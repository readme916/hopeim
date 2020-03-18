标准列表页控制器
-----------
* 企业端通用数据列表控制器ListController /v1/list/{entity},数据如果实现了IOrgEntity接口 ，**控制器会自动筛选本企业的数据**，并且数据列表只有简单属性

* 例如 /v1/list/role

```json
{
    "items": [
        {
            "createdAt": "2020-03-17 22:27:54",
            "code": "platform_employee",
            "name": "平台员工",
            "terminal": null,
            "version": 0,
            "uuid": 5,
            "updatedAt": "2020-03-17 22:27:54"
        },
        {
            "createdAt": "2020-03-17 22:27:54",
            "code": "platform_super",
            "name": "平台超管",
            "terminal": null,
            "version": 0,
            "uuid": 4,
            "updatedAt": "2020-03-17 22:27:54"
        },
        {
            "createdAt": "2020-03-17 22:27:54",
            "code": "platform_manager",
            "name": "平台管理员",
            "terminal": null,
            "version": 0,
            "uuid": 3,
            "updatedAt": "2020-03-17 22:27:54"
        },
        {
            "createdAt": "2020-03-17 18:53:17",
            "code": "user",
            "name": "普通用户",
            "terminal": "user",
            "version": 0,
            "uuid": 2,
            "updatedAt": "2020-03-17 18:53:17"
        },
        {
            "createdAt": "2020-03-17 18:53:17",
            "code": "developer",
            "name": "开发者",
            "terminal": "org",
            "version": 0,
            "uuid": 1,
            "updatedAt": "2020-03-17 18:53:17"
        }
    ],
    "total": 5,
    "pageNumber": 0,
    "pageSize": 20
}
```


* 通用数据列表url获取数据也可以使用筛选项 /v1/list/role?terminal=org

```json
{
    "items": [
        {
            "createdAt": "2020-03-17 18:53:17",
            "code": "developer",
            "name": "开发者",
            "terminal": "org",
            "version": 0,
            "uuid": 1,
            "updatedAt": "2020-03-17 18:53:17"
        }
    ],
    "total": 1,
    "pageNumber": 0,
    "pageSize": 20
}
```


* 如果有不同的需求，例如加入对象属性，可以自己实现路由@RequestMapping ，例子见ListController /v1/list/user

```json

{
    "items": [
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
            "updatedAt": "2020-03-17 19:01:02"
        }
    ],
    "total": 1,
    "pageNumber": 0,
    "pageSize": 20
}
```