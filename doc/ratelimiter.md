限流控制
-------------

系统内部，多个地方应用了限流控制，防止恶意和双击等事件，例如短信接口，登录接口，状态机事件提交接口等

如果需要，用户可自行定制限流功能

例子,防止双击:

```java

	@Transactional
	public void dispatchEvent(Long id, String eventCode) {
		
		//事件的频率限制一次/秒
		RateLimiter rateLimiter = rateLimiterService.get(RateLimiterNamespace.STATEMACHINE_EVENT, getServiceEntity()+id+eventCode, 1);
		rateLimiter.acquire();
		
		StateMachine<String, String> acquireStateMachine = this.acquireStateMachine(id);
		boolean success = acquireStateMachine.sendEvent(eventCode);
		Object error = acquireStateMachine.getExtendedState().getVariables().get("error");
		if (success == false || error != null) {
			throw new BusinessException(3000, "不能执行");
		}
		
	}

```

登录提交频率限制：

```java

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = (authentication.getPrincipal() == null) ? "" : authentication.getName();
		String password = (String) authentication.getCredentials();
		String type = request.getParameter("type");

		if (type == null || "".equals(username)) {
			throw new BadCredentialsException("类型不能为空");
		}
		if (username == null || "".equals(username)) {
			throw new BadCredentialsException("手机号不能为空");
		}

		if (password == null || "".equals(password)) {
			throw new BadCredentialsException("密码不能为空");
		}

		// 每个手机号码，登录的频率限制5秒一次
		RateLimiter rateLimiter = rateLimiterService.get(RateLimiterNamespace.LOGIN, username, 0.2);
		rateLimiter.acquire();
		
	}

```