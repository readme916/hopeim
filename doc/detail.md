列表和详细页的获取
-----------

* 企业端如果使用通用数据列表url获取数据  /v1/list/role ，并且数据实现了IOrgEntity接口 ，控制器会自动筛选本企业的数据，并且数据列表只有简单属性

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


* 通用数据列表url获取数据可以使用筛选项 /v1/list/role?terminal=org

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