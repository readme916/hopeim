Oauth2登录
------


### 标准的Oauth2协议

```xml

<dependency>
	<groupId>org.springframework.security.oauth</groupId>
	<artifactId>spring-security-oauth2</artifactId>
	<version>2.3.5.RELEASE</version>
</dependency>
		
```


### 授权服务器

* oauth2登录服务器独立,默认端口40000

* 默认jdbc数据源的client_id ,client_secret

```java

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetails());
	}

	@Bean
	public ClientDetailsService clientDetails() {
		return new JdbcClientDetailsService(dataSource);
	}

```
* 支持 refresh_token , code , password 的登录  grant_type

* 初始化两个client

```java
	Oauth2Client oauth2ClientPlatform = new Oauth2Client();
	oauth2ClientPlatform.setAccessTokenValidity(86400 * 30);
	oauth2ClientPlatform.setAutoapprove("true");
	oauth2ClientPlatform.setAuthorizedGrantTypes("refresh_token,password");
	oauth2ClientPlatform.setClientId("org");
	oauth2ClientPlatform.setClientSecret(
			PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("org@tianyoukeji"));
	oauth2ClientPlatform.setRefreshTokenValidity(86400 * 365);
	oauth2ClientPlatform.setResourceIds(RoleTemplate.Terminal.org.toString());
	oauth2ClientPlatform.setScope("all");
	oauth2ClientRepository.save(oauth2ClientPlatform);
	Oauth2Client oauth2ClientApp = new Oauth2Client();
	oauth2ClientApp.setAccessTokenValidity(86400 * 30);
	oauth2ClientApp.setAutoapprove("true");
	oauth2ClientApp.setAuthorizedGrantTypes("refresh_token,password");
	oauth2ClientApp.setClientId("user");
	oauth2ClientApp.setClientSecret(
			PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("user@tianyoukeji"));
	oauth2ClientApp.setRefreshTokenValidity(86400 * 365);
	oauth2ClientApp.setResourceIds(RoleTemplate.Terminal.user.toString());
	oauth2ClientApp.setScope("all");
	oauth2ClientRepository.save(oauth2ClientApp);
```

* 初始化一个admin用户,角色为developer,可以登录org客户端

```	
	username:admin
	password:admin
```

* 扩展的登录字段
使用oauth2标准协议登录时候，默认字段只有username，password，无法满足用手机密码和手机验证码两种方式登录的需求
所以我们添加一个提交的字段 type: sms/pwd

同时，我们扩展了提交成功的返回值，添加了unionID，userSig（TIM的通讯签名），userID
![图片1](1584447034.png)
![图片2](1584446601.png)

* 一个web的oauth2登录的例子 [http://localhost:40000/unionLogin]
* 登录后查看自己的info   /v1/detail/myInfo

