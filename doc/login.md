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


