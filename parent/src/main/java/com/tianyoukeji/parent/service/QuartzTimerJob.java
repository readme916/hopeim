package com.tianyoukeji.parent.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import com.tianyoukeji.parent.annotation.StateMachineAction;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.base.IStateMachineEntity;

@Service
public class QuartzTimerJob implements Job {
	
	
	// quartz job 的执行函数
	@Override
	public void execute(JobExecutionContext context) {
		String id = context.getJobDetail().getJobDataMap().get("id").toString();
		String action = context.getJobDetail().getJobDataMap().getString("action");
		String entity = context.getJobDetail().getJobDataMap().getString("entity");
		
		StateMachineService stateMachineService = StateMachineService.services.get(entity);
		
		try {
			Method method = stateMachineService.getClass().getDeclaredMethod(action, Long.class, StateMachine.class);
			StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
			if (annotation == null) {
				throw new BusinessException(1861,
						entity + "服务 ," + action + "的方法，必须使用StateMachineAction注解，才能生效");
			}
			try {
				method.invoke(stateMachineService, Long.valueOf(id), stateMachineService.acquireStateMachine(Long.valueOf(id)));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new BusinessException(1862, entity + "服务 ," + action + "的方法，非法访问");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new BusinessException(1862, entity + "服务 ," + action + "的方法，非法参数");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new BusinessException(2000, e.getCause().getMessage());
			}

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new BusinessException(1861, entity + "服务，没有" + action + "的方法");
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new BusinessException(1861, entity + "服务，禁止访问" + action + "方法");
		}
	}

}
