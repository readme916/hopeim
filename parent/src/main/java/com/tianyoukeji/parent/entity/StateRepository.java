package com.tianyoukeji.parent.entity;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tianyoukeji.parent.entity.State.StateType;


public interface StateRepository extends JpaRepository<State, Long> {

	Set<State> findByEntity(String entityName);
	State findByEntityAndStateType(String entityName, StateType stateType);
	State findByEntityAndCode(String entityName , String code);
}
