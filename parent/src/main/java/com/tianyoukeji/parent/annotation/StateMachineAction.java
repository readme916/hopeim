package com.tianyoukeji.parent.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.liyang.jpa.smart.query.config.JpaSmartQueryAutoConfiguration;
import com.liyang.jpa.smart.query.service.ApplicationContextSupport;


@Retention(RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Transactional
public @interface StateMachineAction {

}
